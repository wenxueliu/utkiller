package com.ut.killer.execute;


import arthas.VmTool;
import com.alibaba.bytekit.utils.IOUtils;
import com.ut.killer.utils.ClassLoaderUtils;
import com.ut.killer.utils.SearchUtils;
import com.ut.killer.utils.VmToolUtils;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstanceUtils {
    private static final Logger logger = LoggerFactory.getLogger(InstanceUtils.class);

    private String className;

    private String hashCode = null;
    private String classLoaderClass;

    private String libPath;
    private static String defaultLibPath;
    private static VmTool vmTool = null;

    static {
        String libName = VmToolUtils.detectLibName();
        if (libName != null) {
            // 这里只能是 /，否则会导致读取不到
            defaultLibPath = "jni/" + libName;
        }
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public void setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
    }

    public void setLibPath(String path) {
        libPath = path;
    }

    public Object getInstance() {
        try {
            Instrumentation inst = ByteBuddyAgent.install();
            if (className == null) {
                throw new IllegalArgumentException("The className option cannot be empty!");
            }
            ClassLoader classLoader = null;
            if (hashCode != null) {
                classLoader = ClassLoaderUtils.getClassLoader(inst, hashCode);
                if (classLoader == null) {
                    throw new IllegalArgumentException("Can not find classloader with hashCode: " + hashCode + ".");
                }
            } else if (classLoaderClass != null) {
                List<ClassLoader> matchedClassLoaders = ClassLoaderUtils.getClassLoaderByClassName(inst,
                        classLoaderClass);
                if (matchedClassLoaders.size() == 1) {
                    classLoader = matchedClassLoaders.get(0);
                    hashCode = Integer.toHexString(matchedClassLoaders.get(0).hashCode());
                } else if (matchedClassLoaders.size() > 1) {
                    throw new IllegalArgumentException(
                            "Found more than one classloader by class name, please specify classloader with '-c <classloader hash>'");
                } else {
                    throw new IllegalArgumentException("Can not find classloader by class name: " + classLoaderClass + ".");
                }
            } else {
                classLoader = ClassLoader.getSystemClassLoader();
            }

            List<Class<?>> matchedClasses = new ArrayList<Class<?>>(
                    SearchUtils.searchClassOnly(inst, className, false, hashCode));
            int matchedClassSize = matchedClasses.size();
            if (matchedClassSize == 0) {
                return null;
            } else if (matchedClassSize > 1) {
                throw new IllegalArgumentException("Found more than one class: " + matchedClasses + ", please specify classloader with '-c <classloader hash>'");
            } else {
                Object[] instances = vmToolInstance().getInstances(matchedClasses.get(0));
                if (instances.length > 1) {
                    throw new IllegalArgumentException("Found more than one instance of class: " + matchedClasses.get(0) + ", please specify classloader with '-c <classloader hash>'");
                }
                return instances[0];
            }
        } catch (Throwable e) {
            logger.error("vmtool error", e);
            throw new IllegalArgumentException("vmtool error: " + e.getMessage());
        }
    }

    private VmTool vmToolInstance() {
        if (vmTool != null) {
            return vmTool;
        } else {
            if (libPath == null) {
                libPath = defaultLibPath;
            }

            // 尝试把lib文件复制到临时文件里，避免多次attach时出现 Native Library already loaded in another classloader
            FileOutputStream tmpLibOutputStream = null;
            InputStream libInputStream = null;
            try {
                File tmpLibFile = File.createTempFile(VmTool.JNI_LIBRARY_NAME, null);
                tmpLibOutputStream = new FileOutputStream(tmpLibFile);
                libInputStream = InstanceUtils.class.getClassLoader().getResourceAsStream(libPath);
                if (Objects.nonNull(libInputStream)) {
                    IOUtils.copy(libInputStream, tmpLibOutputStream);
                    libPath = tmpLibFile.getAbsolutePath();
                    logger.debug("copy {} to {}", libPath, tmpLibFile);
                } else {
                    logger.error("can not find lib: {}", libPath);
                }
            } catch (Throwable e) {
                logger.error("try to copy lib error! libPath: {}", libPath, e);
            } finally {
                IOUtils.close(libInputStream);
                IOUtils.close(tmpLibOutputStream);
            }

            vmTool = VmTool.getInstance(libPath);
        }
        return vmTool;
    }

    public static Object springContextInstance() {
        InstanceUtils vmToolCommand = new InstanceUtils();
        vmToolCommand.setClassLoaderClass("org.springframework.boot.loader.LaunchedURLClassLoader");
        vmToolCommand.setClassName("org.springframework.context.ApplicationContext");
        return vmToolCommand.getInstance();
    }

    public static Object springContextInstance(String className) {
        InstanceUtils instanceUtils = new InstanceUtils();
        instanceUtils.setClassLoaderClass("sun.misc.Launcher$AppClassLoader");
        instanceUtils.setClassName(className);
        return instanceUtils.getInstance();
    }
}

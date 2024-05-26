package com.ut.killer.execute;


import arthas.VmTool;
import com.alibaba.bytekit.utils.IOUtils;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

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
            CodeSource codeSource = InstanceUtils.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                try {
                    File bootJarPath = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                    File soFile = new File(bootJarPath.getParentFile(), "lib" + File.separator + libName);
                    if (soFile.exists()) {
                        defaultLibPath = soFile.getAbsolutePath();
                    }
                } catch (Throwable e) {
                    logger.error("can not find VmTool so", e);
                }
            }
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
//            Instrumentation inst = HotSwapAgentMain.startAgentAndGetInstrumentation();
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
//                        Collection<ClassLoaderVO> classLoaderVOList = ClassUtils
//                                .createClassLoaderVOList(matchedClassLoaders);

//                        VmToolModel vmToolModel = new VmToolModel().setClassLoaderClass(classLoaderClass)
//                                .setMatchedClassLoaders(classLoaderVOList);
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
//                throw new IllegalArgumentException("Can not find class by class name: " + className + ".");
                return null;
            } else if (matchedClassSize > 1) {
                throw new IllegalArgumentException("Found more than one class: " + matchedClasses + ", please specify classloader with '-c <classloader hash>'");
            } else {
                Object[] instances = vmToolInstance().getInstances(matchedClasses.get(0));
                if (instances.length > 1) {
                    throw new IllegalArgumentException("Found more than one instance of class: " + matchedClasses.get(0) + ", please specify classloader with '-c <classloader hash>'");
                }
                Object value = instances[0];
                return value;
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
            libPath = "D:\\下载\\arthas-packaging-3.7.2-bin\\lib\\libArthasJniLibrary-x64.dll";
            if (libPath == null) {
                libPath = defaultLibPath;
            }

            // 尝试把lib文件复制到临时文件里，避免多次attach时出现 Native Library already loaded in another classloader
            FileOutputStream tmpLibOutputStream = null;
            FileInputStream libInputStream = null;
            try {
                File tmpLibFile = File.createTempFile(VmTool.JNI_LIBRARY_NAME, null);
                tmpLibOutputStream = new FileOutputStream(tmpLibFile);
                libInputStream = new FileInputStream(libPath);

                IOUtils.copy(libInputStream, tmpLibOutputStream);
                libPath = tmpLibFile.getAbsolutePath();
                logger.debug("copy {} to {}", libPath, tmpLibFile);
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

package com.ut.killer.execute;


import com.ut.killer.utils.ClassLoaderUtils;
import com.ut.killer.utils.SearchUtils;
import com.ut.killer.utils.VmToolUtils;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstanceUtils {
    private static final Logger logger = LoggerFactory.getLogger(InstanceUtils.class);

    private String className;

    private String hashCode = null;
    private String classLoaderClass;

    public void setClassName(String className) {
        this.className = className;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public void setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
    }

    public Object getInstance() {
        try {
            Instrumentation inst = ByteBuddyAgent.install();
            validateClassName();
            if (hashCode == null) {
                hashCode = getHashCode(inst);
            }
            List<Class<?>> matchedClasses = new ArrayList<>(
                    SearchUtils.searchClassOnly(inst, className, false, hashCode));
            int matchedClassSize = matchedClasses.size();
            if (matchedClassSize == 0) {
                return null;
            } else if (matchedClassSize > 1) {
                throw new IllegalArgumentException("Found more than one class: " + matchedClasses + ", please specify classloader with '-c <classloader hash>'");
            } else {
                return getUniqueInstance(matchedClasses);
            }
        } catch (Throwable e) {
            logger.error("vmtool error", e);
            throw new IllegalArgumentException("vmtool error: " + e.getMessage());
        }
    }

    private Object getUniqueInstance(List<Class<?>> matchedClasses) {
        Object[] instances = VmToolUtils.getVmToolInstance().getInstances(matchedClasses.get(0));
        if (instances.length > 1) {
            throw new IllegalArgumentException("Found more than one instance of class: " + matchedClasses.get(0) + ", please specify classloader with '-c <classloader hash>'");
        }
        return instances[0];
    }

    private String getHashCode(Instrumentation inst) {
        if (Objects.nonNull(classLoaderClass)) {
            List<ClassLoader> matchedClassLoaders = ClassLoaderUtils.getClassLoaderByClassName(inst,
                    classLoaderClass);
            if (matchedClassLoaders.size() == 1) {
                return Integer.toHexString(matchedClassLoaders.get(0).hashCode());
            } else if (matchedClassLoaders.size() > 1) {
                throw new IllegalArgumentException(
                        "Found more than one classloader by class name, please specify classloader with '-c <classloader hash>'");
            } else {
                throw new IllegalArgumentException("Can not find classloader by class name: " + classLoaderClass + ".");
            }
        } else {
            throw new IllegalArgumentException("both classloader and hashCode is null");
        }
    }

    private void validateClassName() {
        if (Objects.isNull(className)) {
            throw new IllegalArgumentException("The className option cannot be empty!");
        }
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

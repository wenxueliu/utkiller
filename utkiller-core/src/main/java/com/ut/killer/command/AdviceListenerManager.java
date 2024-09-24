package com.ut.killer.command;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AdviceListenerManager {
    private static final Logger logger = LoggerFactory.getLogger(AdviceListenerManager.class);

    private static final Map<ClassLoader, MethodAdviceListenerManager> classLoader2AdviceManager = new ConcurrentHashMap<>();

    public static void registerAdviceListener(ClassLoader classLoader, String className, String methodName,
                                              String methodDesc, AdviceListener listener) {
        classLoader = wrap(classLoader);
        className = className.replace('/', '.');

        synchronized (AdviceListenerManager.class) {
            MethodAdviceListenerManager manager = classLoader2AdviceManager.get(classLoader);
            if (Objects.isNull(manager)) {
                manager = new MethodAdviceListenerManager();
                classLoader2AdviceManager.put(classLoader, manager);
            }
            manager.registerAdviceListener(className, methodName, methodDesc, listener);
        }
    }

    public static void updateAdviceListeners() {
    }

    public static List<AdviceListener> queryAdviceListeners(ClassLoader classLoader, String className,
                                                            String methodName, String methodDesc) {
        classLoader = wrap(classLoader);
        className = className.replace('/', '.');
        MethodAdviceListenerManager manager = classLoader2AdviceManager.get(classLoader);
        if (Objects.nonNull(manager)) {
            return manager.queryAdviceListeners(className, methodName, methodDesc);
        }
        return null;
    }

    public static void registerTraceAdviceListener(ClassLoader classLoader, String className, String owner,
                                                   String methodName, String methodDesc, AdviceListener listener) {
        classLoader = wrap(classLoader);
        className = className.replace('/', '.');

        synchronized (AdviceListenerManager.class) {
            MethodAdviceListenerManager manager = classLoader2AdviceManager.get(classLoader);
            if (Objects.isNull(manager)) {
                manager = new MethodAdviceListenerManager();
                classLoader2AdviceManager.put(classLoader, manager);
            }
            manager.registerTraceAdviceListener(className, owner, methodName, methodDesc, listener);
        }
    }

    public static List<AdviceListener> queryTraceAdviceListeners(ClassLoader classLoader, String className,
                                                                 String owner, String methodName, String methodDesc) {
        classLoader = wrap(classLoader);
        className = className.replace('/', '.');
        MethodAdviceListenerManager manager = classLoader2AdviceManager.get(classLoader);
        if (Objects.nonNull(manager)) {
            return manager.queryTraceAdviceListeners(className, owner, methodName, methodDesc);
        }
        return null;
    }

    private static ClassLoader wrap(ClassLoader classLoader) {
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}

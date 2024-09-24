package com.ut.killer.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MethodAdviceListenerManager {
    private ConcurrentHashMap<String, List<AdviceListener>> method2Listeners = new ConcurrentHashMap<>();

    private String key(String className, String methodName, String methodDesc) {
        return className + methodName + methodDesc;
    }

    private String keyForTrace(String className, String owner, String methodName, String methodDesc) {
        return className + owner + methodName + methodDesc;
    }

    public void registerAdviceListener(String className, String methodName, String methodDesc,
                                       AdviceListener listener) {
        String normalizedClassName = className.replace('/', '.');
        String key = key(normalizedClassName, methodName, methodDesc);
        synchronized (this) {
            List<AdviceListener> listeners = method2Listeners.computeIfAbsent(key, k -> new ArrayList<>());
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public List<AdviceListener> queryAdviceListeners(String className, String methodName, String methodDesc) {
        className = className.replace('/', '.');
        String key = key(className, methodName, methodDesc);
        return method2Listeners.get(key);
    }

    public void registerTraceAdviceListener(String className, String owner, String methodName, String methodDesc,
                                            AdviceListener listener) {

        className = className.replace('/', '.');
        String key = keyForTrace(className, owner, methodName, methodDesc);

        synchronized (this) {
            List<AdviceListener> listeners = method2Listeners.computeIfAbsent(key, k -> new ArrayList<>());
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public List<AdviceListener> queryTraceAdviceListeners(String className, String owner, String methodName,
                                                          String methodDesc) {
        String normalizedClassName = className.replace('/', '.');
        String key = keyForTrace(normalizedClassName, owner, methodName, methodDesc);
        return method2Listeners.get(key);
    }
}

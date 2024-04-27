package com.ut.killer.classinfo;

import java.util.HashMap;
import java.util.Map;

public class ClassInfo {
    private String className;

    private final Map<String, MethodInfo> methods = new HashMap<>();

    public void setClassName(String className) {
        this.className = className;
    }

    public void addMethod(MethodInfo method) {
        this.methods.put(method.getSignature(), method);
    }

    public MethodInfo getMethod(String methodSignature) {
        return methods.get(methodSignature);
    }

    public String getClassName() {
        return className;
    }

    public Map<String, MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, MethodInfo> methods) {
        this.methods.putAll(methods);
    }
}

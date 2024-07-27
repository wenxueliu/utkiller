package com.ut.killer.http.request;

import java.util.HashSet;
import java.util.Set;

public class MethodDependencyRequest {
    private String classPath;

    private String methodName;

    private String methodSignature;

    private Set<String> includeClassPaths = new HashSet<>();

    private Set<String> excludeClassPaths = new HashSet<>();


    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public Set<String> getIncludeClassPaths() {
        return includeClassPaths;
    }

    public void setIncludeClassPaths(Set<String> includeClassPaths) {
        this.includeClassPaths = includeClassPaths;
    }

    public Set<String> getExcludeClassPaths() {
        return excludeClassPaths;
    }

    public void setExcludeClassPaths(Set<String> excludeClassPaths) {
        this.excludeClassPaths = excludeClassPaths;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }
}

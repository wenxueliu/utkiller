package com.ut.killer.http.request;

import java.util.Set;

public class MethodDependencyRequest {
    private String classPath;

    private String methodName;

    private Set<String> includeClassPaths;

    private Set<String> excludeClassPaths;


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
}

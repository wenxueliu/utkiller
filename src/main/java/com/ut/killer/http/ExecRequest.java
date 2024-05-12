package com.ut.killer.http;

import java.util.List;

public class ExecRequest {
    String className;

    String methodName;

    String methodSignature;

    List<String> parameterJsonString;

    List<String> parameterTypeSignature;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public List<String> getParameterJsonString() {
        return parameterJsonString;
    }

    public void setParameterJsonString(List<String> parameterJsonString) {
        this.parameterJsonString = parameterJsonString;
    }

    public List<String> getParameterTypeSignature() {
        return parameterTypeSignature;
    }

    public void setParameterTypeSignature(List<String> parameterTypeSignature) {
        this.parameterTypeSignature = parameterTypeSignature;
    }
}
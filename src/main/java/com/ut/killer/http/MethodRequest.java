package com.ut.killer.http;

import java.util.Objects;

public class MethodRequest {
    private String className;

    private String methodName;

    private String methodSignature;

    public MethodRequest() {
    }

    public MethodRequest(String className, String methodName, String methodSignature) {
        this.className = className;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setClassName(String className) {
        this.className = className;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodRequest) {
            MethodRequest methodRequest = (MethodRequest) obj;
            return methodRequest.getClassName().equals(className) && methodRequest.getMethodName().equals(methodName) && methodRequest.getMethodSignature().equals(methodSignature);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, methodSignature);
    }

    public String toString() {
        return "MethodRequest [className=" + className + ", methodName=" + methodName + ", methodSignature=" + methodSignature + "]";
    }
}

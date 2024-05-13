package com.ut.killer.http;

import java.util.Objects;

public class MockRequest {
    private String className;

    private String methodName;

    private String methodSignature;

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MockRequest) {
            MockRequest mockRequest = (MockRequest) obj;
            return mockRequest.getClassName().equals(this.getClassName()) && mockRequest.getMethodName().equals(this.getMethodName()) && mockRequest.getMethodSignature().equals(this.getMethodSignature());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, methodSignature);
    }
}

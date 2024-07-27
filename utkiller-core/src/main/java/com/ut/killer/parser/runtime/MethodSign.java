package com.ut.killer.parser.runtime;

import javassist.CtMethod;

import java.util.Objects;

public class MethodSign {
    String className;

    String methodName;

    String methodSignature;

    public MethodSign(String className, String methodName, String methodSignature) {
        this.className = className;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
    }

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

    public boolean isEqual(CtMethod method) {
        return method.getDeclaringClass().getName().equals(this.className)
                && method.getMethodInfo().getName().equals(this.getMethodName())
                && method.getMethodInfo().getDescriptor().equals(this.getMethodSignature());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodSign) {
            MethodSign methodSignature = (MethodSign) obj;
            return methodSignature.getClassName().equals(this.getClassName())
                    && methodSignature.getMethodName().equals(this.getMethodName())
                    && methodSignature.getMethodSignature().equals(this.getMethodSignature());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, methodSignature);
    }

    @Override
    public String toString() {
        return "MethodSignature{" + "className='" + className + '\'' + ", methodName='" + methodName + '\'' + ", methodSignature='" + methodSignature + '\'' + '}';
    }
}
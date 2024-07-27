package com.ut.killer.classinfo;

import java.util.List;

public class MethodInfo {
    private int line;

    private String signature;

    private String name;

    private List<ArgumentInfo> arguments;

    private ReturnInfo returnInfo;

    private List<MethodInfo> invokeMethods;

    public void setArguments(List<ArgumentInfo> arguments) {
        this.arguments = arguments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReturnInfo(ReturnInfo returnInfo) {
        this.returnInfo = returnInfo;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<ArgumentInfo> getArguments() {
        return arguments;
    }

    public ReturnInfo getReturnInfo() {
        return returnInfo;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public void addArgument(ArgumentInfo argument) {
        this.arguments.add(argument);
    }

    public void setInvokeMethods(List<MethodInfo> invokeMethods) {
        this.invokeMethods = invokeMethods;
    }

    public List<MethodInfo> getInvokeMethods() {
        return invokeMethods;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
package com.ut.killer.parser.sourcecode;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private String name;

    private String returnType;

    private List<String> parameterTypes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}

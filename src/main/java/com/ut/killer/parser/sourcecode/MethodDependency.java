package com.ut.killer.parser.sourcecode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodDependency {
    @JsonProperty("className")
    private String className;

    @JsonProperty("methodName")
    private String methodName;

    @JsonProperty("dependencies")
    private Set<MethodDependency> dependencies = new HashSet<>();

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void addDependency(MethodDependency dependency) {
        dependencies.add(dependency);
    }

    public String getMethodName() {
        return methodName;
    }

    public Set<MethodDependency> getDependencies() {
        return dependencies;
    }
}

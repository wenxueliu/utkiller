package com.ut.killer.parser.sourcecode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodDependency {
    @JsonProperty("className")
    private String className;

    @JsonProperty("methodName")
    private String methodName;

    @JsonProperty("methodSignature")
    private String methodSignature;

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
        this.dependencies.add(dependency);
    }

    public void addDependencies(Set<MethodDependency> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public Set<MethodDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<MethodDependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodDependency) {
            MethodDependency methodDependency = (MethodDependency) obj;
            return methodDependency.getClassName().equals(this.getClassName()) &&
                    methodDependency.getMethodName().equals(this.getMethodName()) &&
                    methodDependency.getMethodSignature().equals(this.getMethodSignature());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, methodSignature);
    }

    public static Set<MethodDependency> flattenDependencies(MethodDependency root) {
        Set<MethodDependency> allDependencies = new HashSet<>();
        Queue<MethodDependency> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            MethodDependency node = queue.poll();
            allDependencies.add(node);
            queue.addAll(node.getDependencies());
        }
        return allDependencies;
    }
}
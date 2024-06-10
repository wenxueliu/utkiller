package com.ut.killer.parser.runtime;

import com.ut.killer.parser.sourcecode.MethodDependency;

import java.util.HashSet;
import java.util.Set;

public class ClassDependency {
    private String className;

    private Set<MethodDependency> methodDependencies = new HashSet<>();

    private Set<ClassDependency> classDependencies = new HashSet<>();

    private Set<MethodDependency> flatMethodDependencies = new HashSet<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void addMethodDependency(MethodDependency methodDependency) {
        this.methodDependencies.add(methodDependency);
    }

    public void addMethodDependencies(Set<MethodDependency> methodDependencies) {
        this.methodDependencies.addAll(methodDependencies);
    }

    public void removeMethodDependency(MethodDependency methodDependency) {
        this.methodDependencies.remove(methodDependency);
    }

    public void removeMethodDependencies(Set<MethodDependency> methodDependencies) {
        this.methodDependencies.removeAll(methodDependencies);
    }

    public Set<ClassDependency> getClassDependencies() {
        return classDependencies;
    }

    public void setClassDependencies(Set<ClassDependency> classDependencies) {
        this.classDependencies = classDependencies;
    }

    public void addClassDependency(ClassDependency classDependency) {
        this.classDependencies.add(classDependency);
    }

    public void removeClassDependency(ClassDependency classDependency) {
        this.classDependencies.remove(classDependency);
    }

    public void setMethodDependencies(Set<MethodDependency> methodDependencies) {
        this.methodDependencies = methodDependencies;
    }

    public Set<MethodDependency> getMethodDependencies() {
        return methodDependencies;
    }

    public void setFlatMethodDependencies(Set<MethodDependency> flatMethodDependencies) {
        this.flatMethodDependencies = flatMethodDependencies;
    }

    public Set<MethodDependency> getFlatMethodDependencies() {
        return flatMethodDependencies;
    }

    public Set<MethodDependency> flattenDependencies() {
        Set<MethodDependency> flatMethodDependencies = new HashSet<>();
        for (MethodDependency methodDependency : methodDependencies) {
            flatMethodDependencies.addAll(MethodDependency.flattenDependencies(methodDependency));
        }
        return flatMethodDependencies;
    }
}
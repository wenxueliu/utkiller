package com.ut.killer.parser.runtime;

import com.ut.killer.parser.sourcecode.MethodDependency;

import java.util.HashSet;
import java.util.Set;

public class ClassDependency {
    String className;

    Set<MethodDependency> methodDependencies = new HashSet<>();

    Set<ClassDependency> classDependencies = new HashSet<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    void addMethodDependency(MethodDependency methodDependency) {
        methodDependencies.add(methodDependency);
    }

    void removeMethodDependency(MethodDependency methodDependency) {
        methodDependencies.remove(methodDependency);
    }

    public Set<ClassDependency> getClassDependencies() {
        return classDependencies;
    }

    public void setClassDependencies(Set<ClassDependency> classDependencies) {
        this.classDependencies = classDependencies;
    }

    void addClassDependency(ClassDependency classDependency) {
        this.classDependencies.add(classDependency);
    }

    void removeClassDependency(ClassDependency classDependency) {
        this.classDependencies.remove(classDependency);
    }

    public void setMethodDependencies(Set<MethodDependency> methodDependencies) {
        this.methodDependencies = methodDependencies;
    }

    public Set<MethodDependency> getMethodDependencies() {
        return methodDependencies;
    }
}
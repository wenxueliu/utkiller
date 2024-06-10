package com.ut.killer.parser.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ut.killer.parser.sourcecode.MethodDependency;
import com.ut.killer.utils.ClazzUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class MethodDependencyParser {
    private static final Logger logger = LoggerFactory.getLogger(MethodDependencyParser.class);

    private final Map<String, MethodDependency> parsedMethods = new HashMap<>();

    private Set<String> excludeDependencies = new HashSet<>();

    private Set<String> includeDependencies = new HashSet<>();

    private final ClassPool pool = ClassPool.getDefault();

    private final Set<String> visitingMethods = new HashSet<>();

    public MethodDependencyParser() {
        this.excludeDependencies.addAll(Arrays.asList("java.lang", "java.util", "java.io", "java.nio",
                "java.net", "java.math", "java.util", "org.slf4j", "javassist", "com.fasterxml",
                "net.bytebuddy", "org.springframework", "org.apache.commons.lang3"));
    }

    public MethodDependencyParser(Set<String> includeDependencies) {
        this.includeDependencies.addAll(includeDependencies);
        this.excludeDependencies.addAll(Arrays.asList("java.lang", "java.util", "java.io"));
    }

    public String parseClassByName(String className) throws NotFoundException, IOException {
        // 替换为你要分析的类的全限定名
        MethodDependencyParser parser = new MethodDependencyParser();
        ClassDependency classDependency = parser.parseClassDependencies(className);
        ObjectMapper mapper = new ObjectMapper();
        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(classDependency);
        System.out.println(jsonOutput);
        return jsonOutput;
    }

    public ClassDependency parseClassDependencies(String className) throws NotFoundException {
        CtClass ctClass = pool.get(className);
        ClassDependency classDependency = new ClassDependency();
        classDependency.setClassName(ctClass.getName());
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            MethodDependency methodDependency = parseMethodDependencies(method);
            classDependency.addMethodDependency(methodDependency);
        }
        return classDependency;
    }

    public ClassDependency parseClassDependencies(String className, String methodName) throws NotFoundException, IOException {
        CtClass ctClass = pool.get(className);
        ClassDependency classDependency = new ClassDependency();
        classDependency.setClassName(ctClass.getName());
        // 解决重载方法，因此用循环
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                MethodDependency methodDependency = parseMethodDependencies(method);
                classDependency.addMethodDependency(methodDependency);
            }
        }
        return classDependency;
    }

    private MethodDependency parseMethodDependencies(CtMethod method) throws NotFoundException {
        String methodId = getMethodId(method);
        if (parsedMethods.containsKey(methodId)) {
            return parsedMethods.get(methodId);
        }
        // 如果递归，则返回一个循环依赖对象
        if (visitingMethods.contains(methodId)) {
            MethodDependency circularDependency = new MethodDependency();
            circularDependency.setClassName(method.getDeclaringClass().getName());
            circularDependency.setMethodName(method.getName());
            return circularDependency;
        }
        visitingMethods.add(methodId);
        MethodDependency methodDependency = new MethodDependency();
        methodDependency.setClassName(method.getDeclaringClass().getName());
        methodDependency.setMethodName(method.getName());
        methodDependency.setMethodSignature(method.getSignature());
        parsedMethods.put(methodId, methodDependency);
        List<MethodSign> methodSignDependencies = getMethodDependencies(method);
        for (MethodSign methodSignDependency : methodSignDependencies) {
            List<CtMethod> dependencyCtMethods = findMethod(methodSignDependency);
            if (!dependencyCtMethods.isEmpty()) {
                for (CtMethod dependencyCtMethod : dependencyCtMethods) {
                    MethodDependency dependency = parseMethodDependencies(dependencyCtMethod);
                    methodDependency.addDependency(dependency);
                }
            } else {
                logger.warn("Method not found: {}", methodSignDependency);
            }
        }
        visitingMethods.remove(methodId);
        return methodDependency;
    }

    private static String getMethodId(CtMethod method) {
        return method.getDeclaringClass().getName() + "." + method.getName() + " " + method.getSignature();
    }

    private List<MethodSign> getMethodDependencies(CtMethod method) {
        List<MethodSign> dependencies = new ArrayList<>();
        List<CtMethod> overrideMethods = findOverrideMethods(method);
        for (CtMethod overrideMethod : overrideMethods) {
            dependencies.addAll(getMethodSignForDependencies(overrideMethod));
        }
        return dependencies;
    }

    private List<MethodSign> getMethodSignForDependencies(CtMethod method) {
        List<MethodSign> dependencies = new ArrayList<>();
        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        ConstPool constPool = methodInfo.getConstPool();
        if (codeAttribute != null) {
            CodeIterator iterator = codeAttribute.iterator();
            while (iterator.hasNext()) {
                try {
                    Optional<MethodSign> methodSign = processInvoke(method, iterator, constPool);
                    methodSign.ifPresent(dependencies::add);
                } catch (BadBytecode ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return dependencies;
    }

    private Optional<MethodSign> processInvoke(CtMethod method, CodeIterator iterator, ConstPool constPool) throws BadBytecode {
        int pos = iterator.next();
        int op = iterator.byteAt(pos);
        if (isMethodInvokeOp(op)) {
            int index = iterator.u16bitAt(pos + 1);
            MethodSign methodSign = buildMethodSign(constPool, index);
            boolean skipAddDependency = isSkipAddDependency(method, methodSign);
            if (skipAddDependency) {
                return Optional.empty();
            }
            return Optional.of(methodSign);
        }
        return Optional.empty();
    }

    private boolean isSkipAddDependency(CtMethod method, MethodSign methodSignature) {
        if (includeDependencies.isEmpty()) {
            return excludeDependencies.stream().anyMatch(excludeDependency -> {
                boolean isMethodStart = methodSignature.getClassName().startsWith(excludeDependency);
                boolean isSelfClass = methodSignature.getClassName().startsWith(method.getDeclaringClass().getName());
                return isMethodStart || isSelfClass;
            });
        } else {
            return includeDependencies.stream().noneMatch(
                    includeDependency -> methodSignature.getClassName().startsWith(includeDependency)
            );
        }
    }

    private static MethodSign buildMethodSign(ConstPool constPool, int index) {
        return new MethodSign(constPool.getMethodrefClassName(index),
                constPool.getMethodrefName(index),
                constPool.getMethodrefType(index));
    }

    private boolean isMethodInvokeOp(int op) {
        return op == CodeIterator.INVOKEVIRTUAL ||
                op == CodeIterator.INVOKESTATIC ||
                op == CodeIterator.INVOKEINTERFACE ||
                op == CodeIterator.INVOKESPECIAL;
    }

    private List<CtMethod> findMethod(MethodSign methodSign) {
        String className = methodSign.getClassName();
        List<CtMethod> findMethods = new ArrayList<>();

        CtClass ctClass;
        try {
            ctClass = pool.get(className);
        } catch (NotFoundException ex) {
            logger.error("pool.get error: ", ex);
            return findMethods;
        }

        // 如果是接口，则需要找到实现该接口的具体类
        if (ctClass.isInterface()) {
            // 获取所有实现该接口的类
            Set<String> implementingClassNames = ClazzUtils.getImplementClassNames(ctClass.getName());
            for (String implementingClassName : implementingClassNames) {
                CtClass implClass;
                try {
                    implClass = pool.get(implementingClassName);
                } catch (NotFoundException ex) {
                    logger.error("pool.get error: ", ex);
                    continue;
                }
                for (CtMethod method : implClass.getDeclaredMethods()) {
                    if (isOverrideMethod(methodSign, method)) {
                        findMethods.add(method);
                    }
                }
            }
        } else {
            // 对于非接口类，按照原来的逻辑处理
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                if (methodSign.isEqual(method)) {
                    findMethods.add(method);
                }
            }
        }
        return findMethods;
    }

    public List<CtMethod> findOverrideMethods(CtMethod method) {
        CtClass ctClass = method.getDeclaringClass();
        List<CtMethod> findMethods = new ArrayList<>();
        if (ctClass.isInterface()) {
            // 获取所有实现该接口的类
            Set<String> implementingClassNames = ClazzUtils.getImplementClassNames(ctClass.getName());
            for (String implementingClassName : implementingClassNames) {
                CtClass implClass;
                try {
                    implClass = pool.get(implementingClassName);
                } catch (NotFoundException ex) {
                    logger.error("pool.get error: ", ex);
                    continue;
                }
                for (CtMethod declaredMethod : implClass.getDeclaredMethods()) {
                    if (declaredMethod.equals(method)) {
                        findMethods.add(declaredMethod);
                    }
                }
            }
        } else {
            findMethods.add(method);
        }
        return findMethods;
    }

    public boolean isOverrideMethod(MethodSign methodSign, CtMethod method) {
        return methodSign.getMethodName().equals(method.getName())
                && methodSign.getMethodSignature().equals(method.getSignature());
    }

    public Set<String> getIncludeDependencies() {
        return includeDependencies;
    }

    void setIncludeDependencies(Set<String> includeDependencies) {
        this.includeDependencies = includeDependencies;
    }

    public Set<String> getExcludeDependencies() {
        return excludeDependencies;
    }

    public void setExcludeDependencies(Set<String> excludeDependencies) {
        this.excludeDependencies = excludeDependencies;
    }

    public void addExcludeDependency(String excludeDependency) {
        this.excludeDependencies.add(excludeDependency);
    }

    public void addExcludeDependencies(Set<String> excludeDependency) {
        this.excludeDependencies.addAll(excludeDependency);
    }

    public void addIncludeDependency(String includeDependency) {
        this.includeDependencies.add(includeDependency);
    }

    public void addIncludeDependencies(Set<String> includeDependency) {
        this.includeDependencies.addAll(includeDependency);
    }

    public void removeIncludeDependency(String includeDependency) {
        this.includeDependencies.remove(includeDependency);
    }

    public void removeIncludeDependencies(Set<String> includeDependency) {
        this.includeDependencies.removeAll(includeDependency);
    }

    public void removeExcludeDependency(String excludeDependency) {
        this.excludeDependencies.remove(excludeDependency);
    }

    public void removeExcludeDependencies(Set<String> excludeDependency) {
        this.excludeDependencies.removeAll(excludeDependency);
    }

    public static void main(String[] args) throws NotFoundException, IOException {
        new MethodDependencyParser().parseClassDependencies("com.ut.killer.parser.runtime.MethodDependencyParser", "parseMethodDependencies");
    }
}
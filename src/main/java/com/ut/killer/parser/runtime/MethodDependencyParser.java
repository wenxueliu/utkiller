package com.ut.killer.parser.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ut.killer.parser.sourcecode.MethodDependency;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.*;

import java.io.IOException;
import java.util.*;

public class MethodDependencyParser {
    private final Map<String, MethodDependency> parsedMethods = new HashMap<>();

    private final Set<String> whiteDependencies = new HashSet<>();

    private final ClassPool pool = ClassPool.getDefault();

    private final Set<String> visitingMethods = new HashSet<>();

    public MethodDependencyParser() {
        this.whiteDependencies.addAll(Arrays.asList("java.lang", "java.util", "java.io"));
    }

    public MethodDependencyParser(List<String> whiteDependencies) {
        this.whiteDependencies.addAll(whiteDependencies);
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
        CtMethod method = ctClass.getDeclaredMethod(methodName);
        MethodDependency methodDependency = parseMethodDependencies(method);
        classDependency.addMethodDependency(methodDependency);
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
        parsedMethods.put(methodId, methodDependency);
        List<MethodSign> dependencies = getMethodDependencies(method);
        for (MethodSign dependencyName : dependencies) {
            try {
                CtMethod dependencyMethod = findMethod(dependencyName);
                if (dependencyMethod != null) {
                    MethodDependency dependency = parseMethodDependencies(dependencyMethod);
                    methodDependency.addDependency(dependency);
                } else {
                    System.out.println("Method not found: " + dependencyName);
                }
            } catch (NotFoundException e) {
                System.out.println("Method not found: " + dependencyName);
            }
        }
        visitingMethods.remove(methodId);
        return methodDependency;
    }

    private static String getMethodId(CtMethod method) {
        return method.getDeclaringClass().getName() + "." + method.getName() + " " + method.getSignature();
    }

    private List<MethodSign> getMethodDependencies(CtMethod method) throws NotFoundException {
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
                } catch (BadBytecode e) {
                    e.printStackTrace();
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
            return Optional.ofNullable(methodSign);
        }
        return Optional.empty();
    }

    private boolean isSkipAddDependency(CtMethod method, MethodSign methodSignature) {
        return whiteDependencies.stream().anyMatch(whiteDependency -> {
            boolean isMethodStart = methodSignature.getClassName().startsWith(whiteDependency);
            boolean isSelfClass = methodSignature.getClassName().startsWith(method.getDeclaringClass().getName());
            return isMethodStart || isSelfClass;
        });
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

    private CtMethod findMethod(MethodSign methodSign) throws NotFoundException {
        String className = methodSign.getClassName();
        CtClass ctClass = pool.get(className);
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (methodSign.isEqual(method)) {
                return method;
            }
        }
        return null;
    }

    public static void main(String[] args) throws NotFoundException, IOException {
        new MethodDependencyParser().parseClassDependencies("com.ut.killer.parser.runtime.MethodDependencyParser", "parseMethodDependencies");
    }
}
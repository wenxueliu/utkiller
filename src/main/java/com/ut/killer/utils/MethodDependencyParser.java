package com.ut.killer.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.*;

import java.util.Objects;

public class MethodDependencyParser {

    public static void main(String[] args) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        String className = "com.ut.killer.utils.MethodDependencyParser"; // 替换为你要分析的类的全名
        CtClass ctClass = pool.get(className);
        analyzeClassMethods(ctClass);
    }

    private static CtClass loadClass(ClassPool pool, String className) throws NotFoundException {
        return pool.get(className);
    }

    private static void analyzeClassMethods(CtClass ctClass) throws NotFoundException {
        CtMethod[] methods = ctClass.getDeclaredMethods();
        for (CtMethod method : methods) {
            System.out.println("=================" + method.getName() + " " + method.getSignature() + "=================");
            analyzeMethod(method);
        }
    }

    private static void analyzeMethod(CtMethod ctMethod) {
        MethodInfo methodInfo = ctMethod.getMethodInfo2();
        if (methodInfo.getAttribute("Code") != null) {
            CodeAttribute codeAttribute = (CodeAttribute) methodInfo.getAttribute("Code");
            ConstPool constPool = methodInfo.getConstPool();
            analyzeBytecode(codeAttribute, constPool);
        }
    }

    private static void analyzeBytecode(CodeAttribute codeAttribute, ConstPool constPool) {
        ExceptionTable exceptions = codeAttribute.getExceptionTable();
        byte[] code = codeAttribute.getCode();

        int index = 0;
        while (index < code.length) {
            int opcode = code[index] & 0xFF; // 获取操作码
            index++; // 移动到下一个字节

            if (isMethodInvocation(opcode)) {
                int cpIndex = ((code[index] & 0xFF) << 8) | (code[index + 1] & 0xFF);
                index += 2;
                printMethodInvocationInfo(constPool, cpIndex);
            }
        }
    }


    private static boolean isMethodInvocation(int opcode) {
        return opcode == Opcode.INVOKEVIRTUAL || opcode == Opcode.INVOKEINTERFACE || opcode == Opcode.INVOKESPECIAL
                || opcode == Opcode.INVOKESTATIC;
    }

    private static void printMethodInvocationInfo(ConstPool constPool, int cpIndex) {
        String calledMethodName = constPool.getMethodrefName(cpIndex);
        String calledMethodClassName = constPool.getMethodrefClassName(cpIndex);
        String calledMethodDesc = constPool.getMethodrefType(cpIndex);
        if (Objects.isNull(calledMethodClassName)) {
            return;
        }
        if (Objects.nonNull(calledMethodClassName) && (calledMethodClassName.startsWith("java.lang") || calledMethodClassName.startsWith("java.io"))) {
            return;
        }
        System.out.println("Class: " + calledMethodClassName + "  Method: " + calledMethodName + "  Descriptor: " + calledMethodDesc);
    }
}

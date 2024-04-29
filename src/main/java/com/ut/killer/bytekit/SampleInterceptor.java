package com.ut.killer.bytekit;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtEnter;
import com.alibaba.bytekit.asm.interceptor.annotation.AtExit;
import com.alibaba.bytekit.asm.interceptor.annotation.AtInvoke;
import com.alibaba.bytekit.asm.interceptor.annotation.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ut.killer.classinfo.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SampleInterceptor {
    public static ThreadLocal<ClassInfo> threadLocal = new ThreadLocal<>();

    @AtEnter(inline = true, suppress = RuntimeException.class, suppressHandler = PrintExceptionSuppressHandler.class)
    public static void atEnter(@Binding.This Object object,
                               @Binding.Class Object clazz,
                               @Binding.Line int line,
                               @Binding.Args Object[] args,
                               @Binding.ArgNames String[] argNames,
                               @Binding.MethodName String methodName,
                               @Binding.MethodDesc String methodDesc) {
        ClassInfo classInfo = threadLocal.get();
        if (Objects.isNull(classInfo)) {
            classInfo = new ClassInfo();
        }
        classInfo.setClassName(object.getClass().getName());
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setLine(line);
        methodInfo.setName(methodName);
        List<ArgumentInfo> argumentInfos = ClassUtils.toArguments(args, argNames);
        methodInfo.setArguments(argumentInfos);
        methodInfo.setSignature(methodDesc);
        classInfo.addMethod(methodInfo);
        threadLocal.set(classInfo);
    }



    @AtExit(inline = true)
    public static void atExit(@Binding.MethodDesc String methodDesc,
                              @Binding.Return Object returnObject) {
        ClassInfo classInfo = threadLocal.get();
        if (Objects.isNull(classInfo)) {
            throw new RuntimeException("classInfo is null");
        }
        MethodInfo methodInfo = classInfo.getMethod(methodDesc);
        ReturnInfo returnInfo = new ReturnInfo();
        returnInfo.setValue(returnObject);
        returnInfo.setType(returnObject.getClass().getName());
        methodInfo.setReturnInfo(returnInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println("classInfo: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(classInfo));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        threadLocal.set(classInfo);
    }

    @AtInvoke(name = "", inline = false, whenComplete = true, excludes = {"System."})
    public static void onInvoke(
//            @Binding.This Object object,
//            @Binding.Class Object clazz,
            @Binding.Line int line,
//            @Binding.InvokeReturn Object invokeReturn,
            @Binding.InvokeMethodDeclaration String declaration
//            @Binding.InvokeArgs Object[] args
    ) {
        ClassInfo classInfo = threadLocal.get();
        if (Objects.isNull(classInfo)) {
            throw new RuntimeException("classInfo is null");
        }
//        System.err.println("onInvoke: line: " + line);
//        System.err.println("onInvoke: this: " + object);
//        System.err.println("declaration: " + declaration);
//        System.err.println("invokeReturn: " + invokeReturn);
    }

    @ExceptionHandler(inline = true)
    public static void onSuppress(@Binding.Throwable Throwable e//,
//                                  @Binding.Class Object clazz
    ) {
        System.out.println("exception handler: " + e);
        e.printStackTrace();
    }

    public static String parseReturnType(String signature) {
        return signature.substring(signature.indexOf(")") + 1);
    }

    public static List<String> parseArgumentType(String signature) {
        String argsStr = signature.substring(signature.indexOf("(") + 1, signature.indexOf(")"));
        return Arrays.asList(argsStr.split(";"));
    }

    public static void main(String[] args) {
        System.out.println("parseArgumentType: " + parseArgumentType("()V"));
        ;
        System.out.println("parseReturnType: " + parseReturnType("()V"));
        System.out.println("parseArgumentType: " + parseArgumentType("(Ljava/lang/String;Z)Ljava/lang/String;"));
        ;
        System.out.println("parseReturnType: " + parseReturnType("(Ljava/lang/String;Z)Ljava/lang/String;"));
    }
}
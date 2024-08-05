package com.ut.killer.command;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.*;
import ut.killer.SpyAPI;

/**
 * @author hengyunabc 2020-06-05
 */
public class SpyInterceptors {

    public static class SpyInterceptor1 {

        @AtEnter(inline = true)
        public static void atEnter(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                   @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.ArgNames String[] argNames) {
            SpyAPI.atEnter(clazz, methodInfo, target, args, argNames);
        }
    }

    public static class SpyInterceptor2 {
        @AtExit(inline = true)
        public static void atExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                  @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.ArgNames String[] argNames, @Binding.Return Object returnObj) {
            SpyAPI.atExit(clazz, methodInfo, target, args, argNames, returnObj);
        }
    }

    public static class SpyInterceptor3 {
        @AtExceptionExit(inline = true)
        public static void atExceptionExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                           @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.ArgNames String[] argNames,
                                           @Binding.Throwable Throwable throwable) {
            SpyAPI.atExceptionExit(clazz, methodInfo, target, args, argNames, throwable);
        }
    }

    public static class SpyTraceInterceptor1 {
        @AtInvoke(name = "", inline = true, whenComplete = false, excludes = {"java.arthas.SpyAPI", "java.lang.Byte"
                , "java.lang.Boolean"
                , "java.lang.Short"
                , "java.lang.Character"
                , "java.lang.Integer"
                , "java.lang.Float"
                , "java.lang.Long"
                , "java.lang.Double"})
        public static void onInvoke(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                    String methodInfo, Object[] args, String[] argNames,
                                    @Binding.InvokeInfo String invokeInfo) {
            SpyAPI.atBeforeInvoke(clazz, methodInfo, args, argNames, invokeInfo, target);
        }
    }

    public static class SpyTraceInterceptor2 {
        @AtInvoke(name = "", inline = true, whenComplete = true, excludes = {"java.arthas.SpyAPI", "java.lang.Byte"
                , "java.lang.Boolean"
                , "java.lang.Short"
                , "java.lang.Character"
                , "java.lang.Integer"
                , "java.lang.Float"
                , "java.lang.Long"
                , "java.lang.Double"})
        public static void onInvokeAfter(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                         String methodInfo, Object[] args, String[] argNames,
                                         @Binding.InvokeInfo String invokeInfo) {
            SpyAPI.atAfterInvoke(clazz, methodInfo, args, argNames, invokeInfo, target);
        }
    }

    public static class SpyTraceInterceptor3 {
        @AtInvokeException(name = "", inline = true, excludes = {"java.arthas.SpyAPI", "java.lang.Byte"
                , "java.lang.Boolean"
                , "java.lang.Short"
                , "java.lang.Character"
                , "java.lang.Integer"
                , "java.lang.Float"
                , "java.lang.Long"
                , "java.lang.Double"})
        public static void onInvokeException(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                             String methodInfo, Object[] args, String[] argNames,
                                             @Binding.InvokeInfo String invokeInfo, @Binding.Throwable Throwable throwable) {
            SpyAPI.atInvokeException(clazz, methodInfo, args, argNames, invokeInfo, target, throwable);
        }
    }

    public static class SpyTraceExcludeJDKInterceptor1 {
        @AtInvoke(name = "", inline = true, whenComplete = false, excludes = "java.**")
        public static void onInvoke(@Binding.This Object target,
                                    @Binding.Class Class<?> clazz,
                                    @Binding.MethodInfo String methodInfo,
                                    @Binding.Args Object[] args,
                                    @Binding.ArgNames String[] argNames,
                                    @Binding.InvokeInfo String invokeInfo) {
            SpyAPI.atBeforeInvoke(clazz, methodInfo, args, argNames, invokeInfo, target);
        }
    }

    public static class SpyTraceExcludeJDKInterceptor2 {
        @AtInvoke(name = "", inline = true, whenComplete = true, excludes = "java.**")
        public static void onInvokeAfter(@Binding.This Object target,
                                         @Binding.Class Class<?> clazz,
                                         @Binding.MethodInfo String methodInfo,
                                         @Binding.Args Object[] args,
                                         @Binding.ArgNames String[] argNames,
                                         @Binding.InvokeInfo String invokeInfo) {
            SpyAPI.atAfterInvoke(clazz, methodInfo, args, argNames, invokeInfo, target);
        }
    }

    public static class SpyTraceExcludeJDKInterceptor3 {
        @AtInvokeException(name = "", inline = true, excludes = "java.**")
        public static void onInvokeException(@Binding.This Object target,
                                             @Binding.Class Class<?> clazz,
                                             @Binding.MethodInfo String methodInfo,
                                             @Binding.Args Object[] args,
                                             @Binding.ArgNames String[] argNames,
                                             @Binding.InvokeInfo String invokeInfo, @Binding.Throwable Throwable throwable) {
            SpyAPI.atInvokeException(clazz, methodInfo, args, argNames, invokeInfo, target, throwable);
        }
    }

}

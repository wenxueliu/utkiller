package com.ut.killer.Interceptor;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtExceptionExit;
import ut.killer.SpyAPI;

public class AtExceptionInterceptor {
    @AtExceptionExit(inline = true)
    public static void atExceptionExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                       @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.ArgNames String[] argNames,
                                       @Binding.Throwable Throwable throwable) {
        SpyAPI.atExceptionExit(clazz, methodInfo, target, args, argNames, throwable);
    }
}

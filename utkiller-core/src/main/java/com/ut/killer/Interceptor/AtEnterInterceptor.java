package com.ut.killer.Interceptor;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtEnter;
import ut.killer.SpyAPI;

public class AtEnterInterceptor {
    @AtEnter(inline = true)
    public static void atEnter(@Binding.This Object target, @Binding.Class Class<?> clazz,
                               @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.ArgNames String[] argNames) {
        SpyAPI.atEnter(clazz, methodInfo, target, args, argNames);
    }
}

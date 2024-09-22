package com.ut.killer.Interceptor;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtExit;
import ut.killer.SpyAPI;

public class AtExitInterceptor {
    @AtExit(inline = true)
    public static void atExit(@Binding.This Object target, @Binding.Class Class<?> clazz,
                              @Binding.MethodInfo String methodInfo, @Binding.Args Object[] args, @Binding.ArgNames String[] argNames, @Binding.Return Object returnObj) {
        SpyAPI.atExit(clazz, methodInfo, target, args, argNames, returnObj);
    }
}

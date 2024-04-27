package com.ut.killer.bytekit;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.ExceptionHandler;

public class PrintExceptionSuppressHandler {
    @ExceptionHandler(inline = false)
    public static void onSuppress(@Binding.Throwable Throwable e, @Binding.Class Object clazz) {
        System.out.println("exception handler: " + clazz);
        e.printStackTrace();
    }
}

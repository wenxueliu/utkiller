package com.ut.killer.command;

import com.ut.killer.classinfo.ArgumentInfo;

import java.util.List;

public interface AdviceListener {
    long id();

    void create();

    void destroy();

    void before(
            Class<?> clazz, String methodName, String methodDesc,
            Object target, Object[] args,  String[] argNames) throws Throwable;


    void afterReturning(
            Class<?> clazz, String methodName, String methodDesc,
            Object target, List<ArgumentInfo> args,
            Object returnObject) throws Throwable;

    void afterThrowing(
            Class<?> clazz, String methodName, String methodDesc,
            Object target, List<ArgumentInfo> args,
            Throwable throwable) throws Throwable;
}

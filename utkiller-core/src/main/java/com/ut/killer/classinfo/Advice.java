package com.ut.killer.classinfo;

import java.util.List;

public class Advice {
    private final ClassLoader loader;
    private final Class<?> clazz;
    private final ArthasMethod method;
    private final Object target;
    private final List<ArgumentInfo> params;
    private final Object returnObj;
    private final Throwable throwExp;
    private final boolean isBefore;
    private final boolean isThrow;
    private final boolean isReturn;

    public boolean isBefore() {
        return isBefore;
    }

    public boolean isAfterReturning() {
        return isReturn;
    }

    public boolean isAfterThrowing() {
        return isThrow;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public Object getTarget() {
        return target;
    }


    public Object getReturnObj() {
        return returnObj;
    }

    public Throwable getThrowExp() {
        return throwExp;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public ArthasMethod getMethod() {
        return method;
    }

    public List<ArgumentInfo> getParams() {
        return params;
    }

    private Advice(
            ClassLoader loader,
            Class<?> clazz,
            ArthasMethod method,
            Object target,
            List<ArgumentInfo> params,
            Object returnObj,
            Throwable throwExp,
            int access) {
        this.loader = loader;
        this.clazz = clazz;
        this.method = method;
        this.target = target;
        this.params = params;
        this.returnObj = returnObj;
        this.throwExp = throwExp;
        isBefore = (access & AccessPoint.ACCESS_BEFORE.getValue()) == AccessPoint.ACCESS_BEFORE.getValue();
        isThrow = (access & AccessPoint.ACCESS_AFTER_THROWING.getValue()) == AccessPoint.ACCESS_AFTER_THROWING.getValue();
        isReturn = (access & AccessPoint.ACCESS_AFTER_RETUNING.getValue()) == AccessPoint.ACCESS_AFTER_RETUNING.getValue();
    }

    public static Advice newForBefore(ClassLoader loader,
                                      Class<?> clazz,
                                      ArthasMethod method,
                                      Object target,
                                      List<ArgumentInfo> params) {
        return new Advice(
                loader,
                clazz,
                method,
                target,
                params,
                null, //returnObj
                null, //throwExp
                AccessPoint.ACCESS_BEFORE.getValue()
        );
    }

    public static Advice newForAfterReturning(ClassLoader loader,
                                              Class<?> clazz,
                                              ArthasMethod method,
                                              Object target,
                                              List<ArgumentInfo> params,
                                              Object returnObj) {
        return new Advice(
                loader,
                clazz,
                method,
                target,
                params,
                returnObj,
                null, //throwExp
                AccessPoint.ACCESS_AFTER_RETUNING.getValue()
        );
    }

    public static Advice newForAfterThrowing(ClassLoader loader,
                                             Class<?> clazz,
                                             ArthasMethod method,
                                             Object target,
                                             List<ArgumentInfo> params,
                                             Throwable throwExp) {
        return new Advice(
                loader,
                clazz,
                method,
                target,
                params,
                null, //returnObj
                throwExp,
                AccessPoint.ACCESS_AFTER_THROWING.getValue()
        );

    }
}

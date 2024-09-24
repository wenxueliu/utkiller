package com.ut.killer.command;

import com.ut.killer.classinfo.ArgumentInfo;
import com.ut.killer.classinfo.ArthasMethod;
import com.ut.killer.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AdviceListenerAdapter implements AdviceListener {
    private static final Logger logger = LoggerFactory.getLogger(AdviceListenerAdapter.class);

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    private long id = ID_GENERATOR.addAndGet(1);

    private boolean verbose;

    @Override
    public long id() {
        return id;
    }

    @Override
    public void create() {
    }

    @Override
    public void destroy() {
    }

    @Override
    final public void before(Class<?> clazz, String methodName, String methodDesc, Object target, Object[] args, String[] argNames)
            throws Throwable {
        List<ArgumentInfo> arguments = ClassUtils.toArguments(args, argNames);
        before(clazz.getClassLoader(), clazz, new ArthasMethod(clazz, methodName, methodDesc), target, arguments);
    }

    @Override
    final public void afterReturning(Class<?> clazz, String methodName, String methodDesc, Object target, List<ArgumentInfo> args,
                                     Object returnObject) throws Throwable {
        afterReturning(clazz.getClassLoader(), clazz, new ArthasMethod(clazz, methodName, methodDesc), target, args,
                returnObject);
    }

    @Override
    final public void afterThrowing(Class<?> clazz, String methodName, String methodDesc, Object target, List<ArgumentInfo> args,
                                    Throwable throwable) throws Throwable {
        afterThrowing(clazz.getClassLoader(), clazz, new ArthasMethod(clazz, methodName, methodDesc), target, args,
                throwable);
    }

    public abstract void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, List<ArgumentInfo> args)
            throws Throwable;

    public abstract void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target,
                                        List<ArgumentInfo> args, Object returnObject) throws Throwable;

    public abstract void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target,
                                       List<ArgumentInfo> args, Throwable throwable) throws Throwable;

    protected boolean isLimitExceeded(int limit, int currentTimes) {
        return currentTimes >= limit;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}

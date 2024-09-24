package com.ut.killer.command;


import com.ut.killer.classinfo.ArgumentInfo;
import com.ut.killer.utils.ClassUtils;
import com.ut.killer.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.killer.SpyAPI;

import java.util.List;

public class SpyImpl extends SpyAPI.AbstractSpy {
    private static final Logger logger = LoggerFactory.getLogger(SpyImpl.class);

    @Override
    public void atEnter(Class<?> clazz, String methodInfo, Object target, Object[] args, String[] argNames) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];
        // TODO listener 只用查一次，放到 thread local里保存起来就可以了！
        List<AdviceListener> listeners = AdviceListenerManager.queryAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    adviceListener.before(clazz, methodName, methodDesc, target, args, argNames);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}", clazz.getName(), methodInfo, e);
                }
            }
        }

    }

    @Override
    public void atExit(Class<?> clazz, String methodInfo, Object target, Object[] args, String[] argNames, Object returnObject) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];

        List<AdviceListener> listeners = AdviceListenerManager.queryAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    List<ArgumentInfo> arguments = ClassUtils.toArguments(args, argNames);
                    adviceListener.afterReturning(clazz, methodName, methodDesc, target, arguments, returnObject);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}", clazz.getName(), methodInfo, e);
                }
            }
        }
    }

    @Override
    public void atExceptionExit(Class<?> clazz, String methodInfo, Object target, Object[] args, String[] argNames, Throwable throwable) {
        ClassLoader classLoader = clazz.getClassLoader();

        String[] info = StringUtils.splitMethodInfo(methodInfo);
        String methodName = info[0];
        String methodDesc = info[1];

        List<AdviceListener> listeners = AdviceListenerManager.queryAdviceListeners(classLoader, clazz.getName(),
                methodName, methodDesc);
        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    List<ArgumentInfo> arguments = ClassUtils.toArguments(args, argNames);
                    adviceListener.afterThrowing(clazz, methodName, methodDesc, target, arguments, throwable);
                } catch (Throwable e) {
                    logger.error("class: {}, methodInfo: {}", clazz.getName(), methodInfo, e);
                }
            }
        }
    }

    @Override
    public void atBeforeInvoke(Class<?> clazz, String methodInfo, Object[] args, String[] argNames,
                               String invokeInfo, Object target) {
        ClassLoader classLoader = clazz.getClassLoader();
        logger.info("before invoke invokeInfo={}", invokeInfo);
        String[] info = StringUtils.splitInvokeInfo(invokeInfo);
        String owner = info[0];
        String methodName = info[1];
        String methodDesc = info[2];

        List<AdviceListener> listeners = AdviceListenerManager.queryTraceAdviceListeners(classLoader, clazz.getName(),
                owner, methodName, methodDesc);

        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    final InvokeTraceable listener = (InvokeTraceable) adviceListener;
                    listener.invokeBeforeTracing(clazz, owner, methodName, methodDesc, args, argNames, Integer.parseInt(info[3]));
                } catch (Throwable e) {
                    logger.error("class: {}, invokeInfo: {}", clazz.getName(), invokeInfo, e);
                }
            }
        }
    }

    @Override
    public void atAfterInvoke(Class<?> clazz, String methodInfo, Object[] args, String[] argNames,
                              String invokeInfo, Object target) {
        ClassLoader classLoader = clazz.getClassLoader();
        logger.info("after invoke invokeInfo={}", invokeInfo);
        String[] info = StringUtils.splitInvokeInfo(invokeInfo);
        String owner = info[0];
        String methodName = info[1];
        String methodDesc = info[2];
        List<AdviceListener> listeners = AdviceListenerManager.queryTraceAdviceListeners(classLoader, clazz.getName(),
                owner, methodName, methodDesc);

        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    final InvokeTraceable listener = (InvokeTraceable) adviceListener;
                    listener.invokeAfterTracing(clazz, owner, methodName, methodDesc, args, argNames, Integer.parseInt(info[3]));
                } catch (Throwable e) {
                    logger.error("class: {}, invokeInfo: {}", clazz.getName(), invokeInfo, e);
                }
            }
        }

    }

    @Override
    public void atInvokeException(Class<?> clazz, String methodInfo, Object[] args, String[] argNames,
                                  String invokeInfo, Object target, Throwable throwable) {
        ClassLoader classLoader = clazz.getClassLoader();
        logger.info("invoke exception invokeInfo={}", invokeInfo);
        String[] info = StringUtils.splitInvokeInfo(invokeInfo);
        String owner = info[0];
        String methodName = info[1];
        String methodDesc = info[2];

        List<AdviceListener> listeners = AdviceListenerManager.queryTraceAdviceListeners(classLoader, clazz.getName(),
                owner, methodName, methodDesc);

        if (listeners != null) {
            for (AdviceListener adviceListener : listeners) {
                try {
                    if (skipAdviceListener(adviceListener)) {
                        continue;
                    }
                    final InvokeTraceable listener = (InvokeTraceable) adviceListener;
                    listener.invokeThrowTracing(clazz, owner, methodName, methodDesc, args, argNames, Integer.parseInt(info[3]));
                } catch (Throwable e) {
                    logger.error("class: {}, invokeInfo: {}", clazz.getName(), invokeInfo, e);
                }
            }
        }
    }

    private static boolean skipAdviceListener(AdviceListener adviceListener) {
        return false;
    }

}
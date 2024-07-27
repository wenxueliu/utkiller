package com.ut.killer.command;


import com.ut.killer.classinfo.Advice;
import com.ut.killer.classinfo.ArthasMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author beiwei30 on 29/11/2016.
 */
public class TraceAdviceListener extends AbstractTraceAdviceListener implements InvokeTraceable {
    private static final Logger logger = LoggerFactory.getLogger(TraceAdviceListener.class);

    /**
     * Constructor
     */
    public TraceAdviceListener(boolean verbose) {
        super();
        super.setVerbose(verbose);
    }

    /**
     * trace 会在被观测的方法体中，在每个方法调用前后插入字节码，所以方法调用开始，结束，抛异常的时候，都会回调下面的接口
     */
    @Override
    public void invokeBeforeTracing(Class<?> clazz, String tracingClassName,
                                    String tracingMethodName, String tracingMethodDesc,
                                    Object[] args, String[] argNames, int tracingLineNumber)
            throws Throwable {
        ClassLoader classLoader = clazz.getClassLoader();

        // normalize className later
        logger.info("invoke tracingClassName {}", tracingClassName);
        ArthasMethod arthasMethod = new ArthasMethod(clazz, tracingMethodDesc, tracingMethodDesc);

        Advice advice = Advice.newForBefore(classLoader, clazz, arthasMethod, null, null);
//        threadLocalTraceEntity(classLoader).tree.begin(advice, tracingLineNumber, true);
    }

    @Override
    public void invokeAfterTracing(Class<?> clazz, String tracingClassName,
                                   String tracingMethodName, String tracingMethodDesc,
                                   Object[] args, String[] argNames, int tracingLineNumber)
            throws Throwable {
        ClassLoader classLoader = clazz.getClassLoader();
//        threadLocalTraceEntity(classLoader).tree.end();
    }

    @Override
    public void invokeThrowTracing(Class<?> clazz, String tracingClassName,
                                   String tracingMethodName, String tracingMethodDesc,
                                   Object[] args, String[] argNames, int tracingLineNumber)
            throws Throwable {
        ClassLoader classLoader = clazz.getClassLoader();
//        threadLocalTraceEntity(classLoader).tree.end(true);
    }
}

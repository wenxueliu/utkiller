package com.ut.killer.command;


import com.ut.killer.classinfo.Advice;
import com.ut.killer.classinfo.ArthasMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceAdviceListener extends AbstractTraceAdviceListener implements InvokeTraceable {
    private static final Logger logger = LoggerFactory.getLogger(TraceAdviceListener.class);
    
    public TraceAdviceListener(boolean verbose) {
        super();
        super.setVerbose(verbose);
    }
    
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

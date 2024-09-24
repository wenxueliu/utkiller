package com.ut.killer.command;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ut.killer.classinfo.Advice;
import com.ut.killer.classinfo.ArgumentInfo;
import com.ut.killer.classinfo.ArthasMethod;
import com.ut.killer.classinfo.TraceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;


public class AbstractTraceAdviceListener extends AdviceListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTraceAdviceListener.class);

    protected final ThreadLocal<TraceEntity> threadBoundEntity = new ThreadLocal<>();

    public AbstractTraceAdviceListener() {
    }

    protected TraceEntity threadLocalTraceEntity(ClassLoader loader) {
        TraceEntity traceEntity = threadBoundEntity.get();
        if (Objects.isNull(traceEntity)) {
            traceEntity = new TraceEntity(loader);
            threadBoundEntity.set(traceEntity);
        }
        return traceEntity;
    }

    @Override
    public void destroy() {
        threadBoundEntity.remove();
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, List<ArgumentInfo> args)
            throws Throwable {
        TraceEntity traceEntity = threadLocalTraceEntity(loader);
        Advice advice = Advice.newForBefore(loader, clazz, method, target, args);
        traceEntity.tree.begin(advice, -1, false);
        traceEntity.deep++;
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, List<ArgumentInfo> args,
                               Object returnObject) throws Throwable {
        Advice advice = Advice.newForAfterReturning(loader, clazz, method, target, args, returnObject);
        threadLocalTraceEntity(loader).tree.end(advice, -1);
        finishing(loader, advice);
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, List<ArgumentInfo> args,
                              Throwable throwable) throws Throwable {
        int lineNumber = -1;
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length != 0) {
            lineNumber = stackTrace[0].getLineNumber();
        }
        Advice advice = Advice.newForAfterThrowing(loader, clazz, method, target, args, throwable);

        threadLocalTraceEntity(loader).tree.end(advice, throwable, lineNumber);
        finishing(loader, advice);
    }

    private void finishing(ClassLoader loader, Advice advice) {
        // 本次调用的耗时
        TraceEntity traceEntity = threadLocalTraceEntity(loader);
        if (traceEntity.deep >= 1) { // #1817 防止deep为负数
            traceEntity.deep--;
        }
        if (traceEntity.deep == 0) {
            try {
//                boolean conditionResult = isConditionMet(command.getConditionExpress(), advice, cost);
                boolean conditionResult = true;
                if (this.isVerbose()) {
//                    logger.info("Condition express: " + command.getConditionExpress() + " , result: " + conditionResult + "\n");
                }
                if (conditionResult) {
                    // TODO: concurrency issues for process.write
                    ObjectMapper objectMapper = new ObjectMapper();
                    logger.info("trace info {} ", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(traceEntity.getModel()));

                    // 是否到达数量限制
//                    if (isLimitExceeded(command.getNumberOfLimit(), 10000)) {
//                         TODO: concurrency issue to abort process
//                        abortProcess(command.getNumberOfLimit());
//                    }
                }
            } catch (Throwable e) {
                logger.warn("trace failed.", e);
            } finally {
                threadBoundEntity.remove();
            }
        }
    }
}

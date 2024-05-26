package com.ut.killer.http.hander;

import com.ut.killer.HotSwapAgentMain;
import com.ut.killer.bytekit.ByteTransformer;
import com.ut.killer.classinfo.ClassUtils;
import com.ut.killer.execute.MethodExecutor;
import com.ut.killer.http.*;
import com.ut.killer.parser.ClazzUtils;
import com.ut.killer.parser.JavaParser;
import fi.iki.elonen.NanoHTTPD;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.util.HotSwapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class InstrumentAndExecutorHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(InstrumentAndExecutorHandler.class);
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            InstrumentAneExecutorRequest instrumentAneExecutorRequest = handleRequest(session, InstrumentAneExecutorRequest.class);
            handleInstrument(instrumentAneExecutorRequest.toInstrumentRequest());
            Object res = handle(instrumentAneExecutorRequest.getExecRequest());
            return response(ResultData.success(res));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object handle(ExecRequest execRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        MethodExecutor executor = new MethodExecutor();
        return executor.execute(execRequest.getClassName(), execRequest.getMethodName(),
                execRequest.getMethodSignature(), execRequest.getParameterJsonString(),
                execRequest.getParameterTypeSignature());
    }

    public void handleInstrument(InstrumentRequest instrumentRequest) throws Exception {
        Set<String> inputClassNames = instrumentRequest.toClassNames();
        Set<String> targetClassNames =
                inputClassNames.stream().map(ClazzUtils::getImplementClassNames).flatMap(Set::stream).collect(Collectors.toSet());
        logger.info("agentmain target classes {}", targetClassNames);
        Set<Class<?>> targetClasses =
                inputClassNames.stream().map(ClazzUtils::getImplementClasses).flatMap(Set::stream).collect(Collectors.toSet());

        Map<String, List<String>> methodNames = instrumentRequest.toClass2Methods();
        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        Instrumentation instrumentation = HotSwapAgentMain.startAgentAndGetInstrumentation();
        instrumentation.addTransformer(new ByteTransformer(targetClassNames, methodNames), true);
        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }

}

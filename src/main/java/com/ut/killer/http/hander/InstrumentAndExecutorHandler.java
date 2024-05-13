package com.ut.killer.http.hander;

import com.ut.killer.HotSwapAgentMain;
import com.ut.killer.bytekit.ByteTransformer;
import com.ut.killer.execute.MethoExecutor;
import com.ut.killer.http.*;
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
        MethoExecutor executor = new MethoExecutor();
        return executor.execute(execRequest.getClassName(), execRequest.getMethodName(),
                execRequest.getMethodSignature(), execRequest.getParameterJsonString(),
                execRequest.getParameterTypeSignature());
    }

    public void handleInstrument(InstrumentRequest instrumentRequest) throws Exception {
//        List<String> inputClassNames = Arrays.asList("com.imagedance.zpai.controller.ImageController",
//                "com.imagedance.zpai.service.ImageService",
//                "javax.servlet.http.HttpServletRequest");
//        Map<String, List<String>> methodNames = new HashMap<>();
//        methodNames.put("com.imagedance.zpai.controller.ImageController", Arrays.asList("deleteCollectImage"));
//        methodNames.put("com.imagedance.zpai.service.ImageService", Arrays.asList("deleteCollectImage"));
//        methodNames.put("com.imagedance.zpai.service.impl.ImageServiceImpl", Arrays.asList("deleteCollectImage"));
//        methodNames.put("javax.servlet.http.HttpServletRequest", Arrays.asList("getHeaderNames"));
//        methodNames.put("org.apache.catalina.connector.RequestFacade", Arrays.asList("getHeaderNames"));
//        methodNames.put("com.imagedance.zpai.service.ImageMetaService", Arrays.asList("deleteCollectImage"));
//        methodNames.put("com.imagedance.zpai.service.impl.ImageMetaServiceImpl", Arrays.asList("deleteCollectImage"));

        Set<String> inputClassNames = instrumentRequest.toClassNames();
        Set<String> targetClassNames =
                inputClassNames.stream().map(JavaParser::getImplementClassNames).flatMap(Set::stream).collect(Collectors.toSet());
        logger.info("agentmain target classes {}", targetClassNames);
        Set<Class<?>> targetClasses =
                inputClassNames.stream().map(JavaParser::getImplementClasses).flatMap(Set::stream).collect(Collectors.toSet());

        Map<String, List<String>> methodNames = instrumentRequest.toClass2Methods();
        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        Instrumentation instrumentation = HotSwapAgentMain.startAgentAndGetInstrumentation();
        instrumentation.addTransformer(new ByteTransformer(targetClassNames, methodNames), true);
        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }

}

package com.ut.killer.http.hander;

import com.ut.killer.HotSwapAgentMain;
import com.ut.killer.bytekit.ByteTransformer;
import com.ut.killer.http.ResultData;
import com.ut.killer.http.TreeRequest;
import com.ut.killer.parser.ClazzUtils;
import com.ut.killer.parser.JavaParser;
import fi.iki.elonen.NanoHTTPD;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.util.HotSwapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

public class InstrumentHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(InstrumentHandler.class);
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            TreeRequest treeRequest = handleRequest(session, TreeRequest.class);
            handle(treeRequest);
            return response(ResultData.success(treeRequest.getMethodRequest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(TreeRequest treeRequest) throws Exception {
        List<String> inputClassNames = Arrays.asList("com.imagedance.zpai.controller.ImageController",
                "com.imagedance.zpai.service.ImageService",
                "javax.servlet.http.HttpServletRequest");
        Map<String, List<String>> methodNames = new HashMap<>();
        methodNames.put("com.imagedance.zpai.controller.ImageController", Arrays.asList("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.ImageService", Arrays.asList("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.impl.ImageServiceImpl", Arrays.asList("deleteCollectImage"));
        methodNames.put("javax.servlet.http.HttpServletRequest", Arrays.asList("getHeaderNames"));
        methodNames.put("org.apache.catalina.connector.RequestFacade", Arrays.asList("getHeaderNames"));
        methodNames.put("com.imagedance.zpai.service.ImageMetaService", Arrays.asList("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.impl.ImageMetaServiceImpl", Arrays.asList("deleteCollectImage"));

//        Class<?>[] targetClasses = new Class<?>[targetClassNames.size()];
//        for (int i = 0; i < targetClassNames.size(); i++) {
//            targetClasses[i] = Class.forName(targetClassNames.get(i));
//        }

        Set<String> targetClassNames =
                inputClassNames.stream().map(ClazzUtils::getImplementClassNames).flatMap(Set::stream).collect(Collectors.toSet());
        logger.info("agentmain target classes {}", targetClassNames);
        Set<Class<?>> targetClasses =
                inputClassNames.stream().map(ClazzUtils::getImplementClasses).flatMap(Set::stream).collect(Collectors.toSet());

        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        Instrumentation instrumentation = HotSwapAgentMain.startAgentAndGetInstrumentation();
        instrumentation.addTransformer(new ByteTransformer(targetClassNames, methodNames), true);
        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }

}

package com.ut.killer;


import com.google.common.collect.Sets;
import com.ut.killer.bytekit.ByteTransformer;
import com.ut.killer.parser.ClazzUtils;
import com.ut.killer.parser.JavaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;
import java.util.stream.Collectors;

public class AgentMain {
    private static final Logger logger = LoggerFactory.getLogger(AgentMain.class);


    public static void premain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        List<String> inputClassNames = Arrays.asList("com.imagedance.zpai.controller.ImageController",
                "com.imagedance.zpai.service.ImageService",
                "javax.servlet.http.HttpServletRequest");
        Map<String, Set<String>> methodNames = new HashMap<>();
        methodNames.put("com.imagedance.zpai.controller.ImageController", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.ImageService", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.impl.ImageServiceImpl", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("javax.servlet.http.HttpServletRequest", Sets.newHashSet("getHeaderNames"));
        methodNames.put("org.apache.catalina.connector.RequestFacade", Sets.newHashSet("getHeaderNames"));
        methodNames.put("com.imagedance.zpai.service.ImageMetaService", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.impl.ImageMetaServiceImpl", Sets.newHashSet("deleteCollectImage"));

//        Class<?>[] targetClasses = new Class<?>[targetClassNames.size()];
//        for (int i = 0; i < targetClassNames.size(); i++) {
//            targetClasses[i] = Class.forName(targetClassNames.get(i));
//        }

        Set<String> targetClassNames =
                inputClassNames.stream().map(ClazzUtils::getImplementClassNames).flatMap(Set::stream).collect(Collectors.toSet());
        logger.info("agentmain target classes {}", targetClassNames);
        Set<Class<?>> targetClasses =
                inputClassNames.stream().map(ClazzUtils::getImplementClasses).flatMap(Set::stream).collect(Collectors.toSet());
        instrumentation.addTransformer(new ByteTransformer(targetClassNames, methodNames), true);
        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }

    public static void agentmain(String agentOps, Instrumentation instrumentation) throws UnmodifiableClassException {
//        List<String> targetClassNames = Arrays.asList("com.imagedance.zpai.controller.ImageController",
//                "com.imagedance.zpai.service.ImageService",
//                "javax.servlet.http.HttpServletRequest",
//                "org.apache.catalina.connector.RequestFacade",
//                "com.imagedance.zpai.service.impl.ImageServiceImpl",
//                "com.imagedance.zpai.service.impl.ImageMetaServiceImpl");
        Map<String, Set<String>> methodNames = new HashMap<>();
        methodNames.put("com.imagedance.zpai.controller.ImageController", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.ImageService", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.impl.ImageServiceImpl", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("javax.servlet.http.HttpServletRequest", Sets.newHashSet("getHeaderNames"));
        methodNames.put("org.apache.catalina.connector.RequestFacade", Sets.newHashSet("getHeaderNames"));
        methodNames.put("com.imagedance.zpai.service.ImageMetaService", Sets.newHashSet("deleteCollectImage"));
        methodNames.put("com.imagedance.zpai.service.impl.ImageMetaServiceImpl", Sets.newHashSet("deleteCollectImage"));
        System.out.println("======> agentmain started: " + agentOps);

//        Class<?>[] targetClasses = new Class<?>[targetClassNames.size()];
//        for (int i = 0; i < targetClassNames.size(); i++) {
//            targetClasses[i] = Class.forName(targetClassNames.get(i));
//        }

        Set<String> targetClassNames = ClazzUtils.getImplementClassNames(agentOps);
        logger.info("agentmain target classes {}", targetClassNames);
        instrumentation.addTransformer(new ByteTransformer(targetClassNames, methodNames), true);
        instrumentation.retransformClasses(ClazzUtils.getImplementClasses(agentOps).toArray(new Class[0]));
    }
}
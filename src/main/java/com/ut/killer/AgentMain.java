package com.ut.killer;


import com.ut.killer.bytekit.ByteTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain {
    public static void agentmain(String agentOps, Instrumentation instrumentation) throws UnmodifiableClassException,ClassNotFoundException {
//        String targetClassName = "com.imagedance.zpai.controller.ImageController";
//        String methodName = "";
        String targetClassName = "test.BaseTask";
        String methodName = "run";
        System.out.println("======> agentmain started: " + agentOps);
        instrumentation.addTransformer(new ByteTransformer(targetClassName, methodName), true);
        instrumentation.retransformClasses(Class.forName(agentOps));
    }
}
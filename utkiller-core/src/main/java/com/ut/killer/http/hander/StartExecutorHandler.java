package com.ut.killer.http.hander;

import com.ut.killer.ClassManager;
import com.ut.killer.bytekit.ByteTransformer;
import com.ut.killer.bytekit.TransformerManager;
import com.ut.killer.execute.MethodExecutor;
import com.ut.killer.http.request.ExecRequest;
import com.ut.killer.http.request.InstrumentAneExecutorRequest;
import com.ut.killer.http.request.InstrumentRequest;
import com.ut.killer.http.response.ResultData;
import com.ut.killer.utils.ClazzUtils;
import fi.iki.elonen.NanoHTTPD;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.util.HotSwapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StartExecutorHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(StartExecutorHandler.class);

    private Instrumentation instrumentation;

    public StartExecutorHandler(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

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
        MethodExecutor executor = new MethodExecutor(this.instrumentation);
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

        Map<String, Set<String>> newClass2MethodNames = instrumentRequest.toClass2Methods();

        for (String targetClassName : newClass2MethodNames.keySet()) {
            if (ClassManager.containsClass(targetClassName)) {
                Set<String> newMethodNames = newClass2MethodNames.get(targetClassName);
                for (String newMethodName : newMethodNames) {
                    Set<String> currentMethodNames = ClassManager.getMethods(targetClassName);
                    if (currentMethodNames.contains(newMethodName)) {
                        newClass2MethodNames.get(targetClassName).remove(newMethodName);
                        if (newClass2MethodNames.get(targetClassName).isEmpty()) {
                            newClass2MethodNames.remove(targetClassName);
                        }
                    } else {
                        currentMethodNames.add(newMethodName);
                    }
                }
            } else {
                ClassManager.putClass(targetClassName, newClass2MethodNames.get(targetClassName));
            }
        }
        if (newClass2MethodNames.isEmpty()) {
            logger.info("all classes {} has been enhanced", targetClassNames);
            return;
        }
        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        ClassFileTransformer classFileTransformer = new ByteTransformer(targetClassNames, newClass2MethodNames);
        TransformerManager.getInstance(instrumentation).addTransformer(classFileTransformer);
        instrumentation.addTransformer(classFileTransformer, true);
        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }
}
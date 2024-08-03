package com.ut.killer.http.hander;

import com.ut.killer.bytekit.TransformerManager;
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

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StopExecutorHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(StopExecutorHandler.class);

    private Instrumentation instrumentation ;

    public StopExecutorHandler(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            InstrumentAneExecutorRequest instrumentAneExecutorRequest = handleRequest(session, InstrumentAneExecutorRequest.class);
            handleInstrument(instrumentAneExecutorRequest.toInstrumentRequest());
            return response(ResultData.success("ok"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleInstrument(InstrumentRequest instrumentRequest) throws Exception {
        Set<String> inputClassNames = instrumentRequest.toClassNames();
        Set<String> targetClassNames =
                inputClassNames.stream().map(ClazzUtils::getImplementClassNames).flatMap(Set::stream).collect(Collectors.toSet());
        logger.info("agentmain target classes {}", targetClassNames);
        Set<Class<?>> targetClasses =
                inputClassNames.stream().map(ClazzUtils::getImplementClasses).flatMap(Set::stream).collect(Collectors.toSet());

        Map<String, Set<String>> newClass2MethodNames = instrumentRequest.toClass2Methods();
        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        TransformerManager.getInstance(instrumentation).destroy();
//        instrumentation.addTransformer(new ByteTransformer(targetClassNames, newClass2MethodNames), true);
//        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }
}
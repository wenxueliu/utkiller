package com.ut.killer.http.hander;

import com.ut.killer.execute.MethodExecutor;
import com.ut.killer.http.request.ExecRequest;
import com.ut.killer.http.ResultData;
import fi.iki.elonen.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class ExecutorHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorHandler.class);
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            ExecRequest execRequest = handleRequest(session, ExecRequest.class);
            Object result = handle(execRequest);
            return response(ResultData.success(result));
        } catch (Exception ex) {
            logger.error("handle error", ex);
            throw new RuntimeException(ex);
        }
    }

    public Object handle(ExecRequest execRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        MethodExecutor executor = new MethodExecutor();
        return executor.execute(execRequest.getClassName(), execRequest.getMethodName(),
                execRequest.getMethodSignature(), execRequest.getParameterJsonString(),
                execRequest.getParameterTypeSignature());
    }
}

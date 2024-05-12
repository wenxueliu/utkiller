package com.ut.killer.http.hander;

import com.ut.killer.execute.MethoExecutor;
import com.ut.killer.http.ExecRequest;
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object handle(ExecRequest execRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        MethoExecutor executor = new MethoExecutor();
//        String body = "{\"userId\": \"123\",\"userName\": \"John Doe\"}";
//        return executor.execute("com.imagedance.zpai.utils.JsonUtils", "toUserInfo",
//                "(Lcom/imagedance/zpai/model/UserInfo;)Ljava/lang/String;",
//                Arrays.asList(body), Arrays.asList("Lcom/imagedance/zpai/model/UserInfo"));
        return executor.execute(execRequest.getClassName(), execRequest.getMethodName(),
                execRequest.getMethodSignature(), execRequest.getParameterJsonString(),
                execRequest.getParameterTypeSignature());
    }
}

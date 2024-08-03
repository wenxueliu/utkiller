package com.ut.killer.http;

import com.ut.killer.http.hander.*;
import com.ut.killer.http.response.ResultData;
import fi.iki.elonen.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;

import static com.ut.killer.http.HttpConstants.JSON_RESPONSE_HEADER;

public class HttpAgentServer extends NanoHTTPD {
    private static final Logger logger = LoggerFactory.getLogger(StopExecutorHandler.class);

    private HashMap<String, HttpHandler> url2Handler = new HashMap<>();



    public HttpAgentServer(int port, Instrumentation inst) {
        super(port);
        addHandler("/rest/v1/start", new StartExecutorHandler(inst));
        addHandler("/rest/v1/stop", new StopExecutorHandler(inst));
        addHandler("/rest/v1/exec", new ExecutorHandler());
        addHandler("/rest/v1/tree", new TreeHandler());
        addHandler("/rest/v1/all", new AllInOneExecutorHandler());
    }

    void addHandler(String url, HttpHandler handler) {
        url2Handler.put(url, handler);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String method = session.getMethod().toString();
        String queryString = session.getQueryParameterString();
        String postData = "";
        try {
            HttpHandler handler = url2Handler.get(uri);
            if (handler == null) {
                Response response = newFixedLengthResponse(ResultData.error("not support"));
                response.addHeader("Content-Type", JSON_RESPONSE_HEADER);
            }
            return url2Handler.get(uri).handle(session);
        } catch (Exception ex) {
            logger.error("server error", ex);
        }
        return newFixedLengthResponse(ResultData.error("uri: " + uri + "\nmethod: " + method
                + "\nqueryString: " + queryString + "\npostData: " + postData));
    }

    public static void begin(Integer port, Instrumentation inst) {
//        Instrumentation inst = HotSwapAgentMain.startAgentAndGetInstrumentation();
        HttpAgentServer httpServer = new HttpAgentServer(port, inst);
        try {
            httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("start agent server in {}", port);
    }
}
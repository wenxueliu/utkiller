package com.ut.killer.http;

import com.ut.killer.http.hander.ExecutorHandler;
import com.ut.killer.http.hander.HttpHandler;
import com.ut.killer.http.hander.InstrumentAndExecutorHandler;
import com.ut.killer.http.hander.InstrumentHandler;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;

import static com.ut.killer.http.HttpConstants.JSON_RESPONSE_HEADER;

public class HttpAgentServer extends NanoHTTPD {
    HashMap<String, HttpHandler> url2Handler = new HashMap<>();

    public HttpAgentServer(int port) {
        super(port);
        addHandler("/rest/v1/tree", new InstrumentHandler());
        addHandler("/rest/v1/exec", new ExecutorHandler());
        addHandler("/rest/v2/exec", new InstrumentAndExecutorHandler());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFixedLengthResponse(ResultData.error("uri: " + uri + "\nmethod: " + method
                + "\nqueryString: " + queryString + "\npostData: " + postData));
    }

    public static void init(int port) {
        HttpAgentServer httpServer = new HttpAgentServer(port);
        try {
            httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("all over");
    }
}

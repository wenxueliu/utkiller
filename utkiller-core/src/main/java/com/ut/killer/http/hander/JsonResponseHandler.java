package com.ut.killer.http.hander;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ut.killer.http.response.ResultData;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public abstract class JsonResponseHandler implements HttpHandler {
    ObjectMapper objectMapper = new ObjectMapper();

    public <T> T handleRequest(NanoHTTPD.IHTTPSession session, Class<T> clazz) {
        Map<String, String> bodyParams = new HashMap<>();
        try {
            session.parseBody(bodyParams);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            throw new RuntimeException(e);
        }
        String requestBodyText = bodyParams.get("postData");

        try {
            T treeRequest = objectMapper.readValue(requestBodyText, clazz);
            return treeRequest;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> NanoHTTPD.Response response(ResultData<T> resultData) {
        NanoHTTPD.Response response = null;
        try {
            response = newFixedLengthResponse(objectMapper.writeValueAsString(resultData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            response = newFixedLengthResponse(ResultData.error(e.getMessage()));
        }
        response.addHeader("Content-Type", "application/json");
        return response;
    }
}
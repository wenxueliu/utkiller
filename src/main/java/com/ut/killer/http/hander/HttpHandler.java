package com.ut.killer.http.hander;

import fi.iki.elonen.NanoHTTPD;

public interface HttpHandler {
    /**
     * 处理HTTP请求。
     *
     * @param session 表示一个HTTP会话，包含请求的所有信息。
     * @return 返回一个NanoHTTPD.Response对象，它包含了HTTP响应的所有信息，如状态码、响应头和响应体。
     */
    NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
}

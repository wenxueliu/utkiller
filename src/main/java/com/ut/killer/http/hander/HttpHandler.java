package com.ut.killer.http.hander;

import fi.iki.elonen.NanoHTTPD;

public interface HttpHandler {
    NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
}

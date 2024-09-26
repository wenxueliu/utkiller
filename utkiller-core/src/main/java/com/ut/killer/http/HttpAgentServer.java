package com.ut.killer.http;

import com.ut.killer.HotSwapAgentMain;
import com.ut.killer.http.hander.*;
import com.ut.killer.http.response.ResultData;
import fi.iki.elonen.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.killer.SpyAPI;
import ut.killer.config.UTKillerConfiguration;
import ut.killer.utils.YamlUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.jar.JarFile;

import static com.ut.killer.http.HttpConstants.JSON_RESPONSE_HEADER;

public class HttpAgentServer extends NanoHTTPD {
    private static final Logger logger = LoggerFactory.getLogger(HttpAgentServer.class);

    private HashMap<String, HttpHandler> url2Handler = new HashMap<>();

    public HttpAgentServer(UTKillerConfiguration config, Instrumentation inst) {
        super(config.getPort());
        addHandler("/rest/v1/start", new StartExecutorHandler(inst));
        addHandler("/rest/v1/stop", new StopExecutorHandler(inst));
        addHandler("/rest/v1/exec", new ExecutorHandler());
        addHandler("/rest/v1/tree", new TreeHandler());
        addHandler("/rest/v1/all", new AllInOneExecutorHandler(inst));
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

    public static void begin(String configPath, Instrumentation inst) throws Throwable {
        UTKillerConfiguration config = YamlUtils.parse(configPath);
        int port = config.getPort();
        String baseDir = config.getBaseDir();
        if (inst == null) {
            inst = HotSwapAgentMain.startAgentAndGetInstrumentation();
        }
        initSpy(inst, baseDir);
        HttpAgentServer httpServer = new HttpAgentServer(config, inst);
        try {
            httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ex) {
            logger.error("http server start error", ex);
            throw new RuntimeException(ex);
        }
        logger.info("start agent server in {}", port);
        try {
            SpyAPI.init();
        } catch (Throwable e) {
            // ignore
        }
    }

    private static void initSpy(Instrumentation instrumentation, String baseDir) throws Throwable {
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        Class<?> spyClass = null;
        if (parent != null) {
            try {
                spyClass = parent.loadClass("java.arthas.SpyAPI");
            } catch (Throwable e) {
                // ignore
            }
        }
        if (spyClass == null) {
            CodeSource codeSource = HttpAgentServer.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                File spyJarFile = new File(getSandboxSpyJarPath(baseDir));
                instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(spyJarFile));
            } else {
                throw new IllegalStateException("can not find utkiller-spy.jar");
            }
        }
    }

    private static String getSandboxSpyJarPath(String utkillerHome) {
        return utkillerHome + File.separator + "utkiller-spy.jar";
    }
}
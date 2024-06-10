package com.ut.killer.http.hander;

import com.ut.killer.http.request.MethodDependencyRequest;
import com.ut.killer.http.response.ResultData;
import com.ut.killer.parser.runtime.ClassDependency;
import com.ut.killer.parser.runtime.MethodDependencyParser;
import fi.iki.elonen.NanoHTTPD;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class TreeHandler extends JsonResponseHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) throws NotFoundException, IOException {
        MethodDependencyRequest methodDependencyRequest = handleRequest(session, MethodDependencyRequest.class);
        MethodDependencyParser methodDependencyParser = getMethodDependencyParser(methodDependencyRequest);
        String className = methodDependencyRequest.getClassPath();
        String methodName = methodDependencyRequest.getMethodName();
        String methodSignature = methodDependencyRequest.getMethodSignature();
        ClassDependency classDependency;
        if (Objects.nonNull(methodSignature)) {
            classDependency = methodDependencyParser.parseClassDependencies(className, methodName, methodSignature);
        } else {
            classDependency = methodDependencyParser.parseClassDependencies(className, methodName);
        }
        classDependency.setFlatMethodDependencies(classDependency.flattenDependencies());
        return response(ResultData.success(classDependency));
    }

    private static MethodDependencyParser getMethodDependencyParser(MethodDependencyRequest methodDependencyRequest) {
        Set<String> includeClassPaths = methodDependencyRequest.getIncludeClassPaths();
        Set<String> excludeClassPaths = methodDependencyRequest.getExcludeClassPaths();

        MethodDependencyParser methodDependencyParser = new MethodDependencyParser();
        if (Objects.nonNull(includeClassPaths) && !includeClassPaths.isEmpty()) {
            methodDependencyParser.addIncludeDependencies(includeClassPaths);
        }
        if (Objects.nonNull(excludeClassPaths) && !excludeClassPaths.isEmpty()) {
            methodDependencyParser.addExcludeDependencies(excludeClassPaths);
        }
        return methodDependencyParser;
    }
}
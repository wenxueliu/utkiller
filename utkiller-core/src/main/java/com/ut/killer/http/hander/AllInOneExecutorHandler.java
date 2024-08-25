package com.ut.killer.http.hander;

import com.ut.killer.bytekit.ByteTransformer;
import com.ut.killer.bytekit.TransformerManager;
import com.ut.killer.execute.MethodExecutor;
import com.ut.killer.http.request.AllInOneRequest;
import com.ut.killer.http.request.ExecRequest;
import com.ut.killer.http.response.ResultData;
import com.ut.killer.parser.runtime.ClassDependency;
import com.ut.killer.parser.runtime.MethodDependencyParser;
import com.ut.killer.parser.sourcecode.MethodDependency;
import fi.iki.elonen.NanoHTTPD;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.util.HotSwapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class AllInOneExecutorHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(AllInOneExecutorHandler.class);

    private Instrumentation instrumentation;

    private Map<String, Set<String>> methodNames = new HashMap<>();

    public AllInOneExecutorHandler(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            AllInOneRequest allInOneRequest = handleRequest(session, AllInOneRequest.class);
            String className = allInOneRequest.getExecRequest().getClassName();
            String methodName = allInOneRequest.getExecRequest().getMethodName();
            String methodSignature = allInOneRequest.getExecRequest().getMethodSignature();
            MethodDependencyParser methodDependencyParser = getMethodDependencyParser(allInOneRequest);
            ClassDependency classDependency;
            if (Objects.nonNull(methodSignature)) {
                classDependency = methodDependencyParser.parseClassDependencies(className, methodName, methodSignature);
            } else {
                classDependency = methodDependencyParser.parseClassDependencies(className, methodName);
            }
            Set<MethodDependency> methodDependencies = classDependency.flattenDependencies();
            handleInstrument(methodDependencies);
            Object res = handle(allInOneRequest.getExecRequest());
            return response(ResultData.success(res));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodDependencyParser getMethodDependencyParser(AllInOneRequest allInOneRequest) {
        Set<String> includeClassPaths = allInOneRequest.getIncludeClassPaths();
        Set<String> excludeClassPaths = allInOneRequest.getExcludeClassPaths();

        MethodDependencyParser methodDependencyParser = new MethodDependencyParser();
        if (Objects.nonNull(includeClassPaths) && !includeClassPaths.isEmpty()) {
            methodDependencyParser.addIncludeDependencies(includeClassPaths);
        }
        if (Objects.nonNull(excludeClassPaths) && !excludeClassPaths.isEmpty()) {
            methodDependencyParser.addExcludeDependencies(excludeClassPaths);
        }
        return methodDependencyParser;
    }

    public Object handle(ExecRequest execRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        MethodExecutor executor = new MethodExecutor(instrumentation);
        return executor.execute(execRequest.getClassName(), execRequest.getMethodName(),
                execRequest.getMethodSignature(), execRequest.getParameterJsonString(),
                execRequest.getParameterTypeSignature());
    }


    Map<String, Set<String>> newClass2MethodNames(Set<MethodDependency> methodDependencies) {
        Map<String, Set<String>> className2MethodNames = new HashMap<>();

        for (MethodDependency methodDependency : methodDependencies) {
            String className = methodDependency.getClassName();
            String methodName = methodDependency.getMethodName();

            Set<String> methods = className2MethodNames.computeIfAbsent(className, k -> new HashSet<>());
            methods.add(methodName);
        }
        return className2MethodNames;
    }

    public void handleInstrument(Set<MethodDependency> methodDependencies) throws Exception {
        Set<String> targetClassNames = methodDependencies.stream().map(MethodDependency::getClassName).collect(Collectors.toSet());
        logger.info("agentmain target classes {}", targetClassNames);
        Set<Class<?>> targetClasses =
                methodDependencies.stream().map(methodDependency -> {
                    try {
                        return Class.forName(methodDependency.getClassName());
                    } catch (ClassNotFoundException ex) {
                        logger.error("Class.forName error", ex);
                        throw new RuntimeException(ex);
                    }
                }).collect(Collectors.toSet());

        Map<String, Set<String>> newClass2MethodNames = newClass2MethodNames(methodDependencies);
        for (String targetClassName : newClass2MethodNames.keySet()) {
            if (methodNames.containsKey(targetClassName)) {
                Set<String> newMethodNames = newClass2MethodNames.get(targetClassName);
                for (String newMethodName : newMethodNames) {
                    if (methodNames.get(targetClassName).contains(newMethodName)) {
                        newClass2MethodNames.get(targetClassName).remove(newMethodName);
                    } else {
                        methodNames.get(targetClassName).add(newMethodName);
                    }
                }
            } else {
                methodNames.put(targetClassName, newClass2MethodNames.get(targetClassName));
            }
        }
        if (newClass2MethodNames.isEmpty() || newClass2MethodNames.values().isEmpty()) {
            return;
        }
        boolean isEmpty = true;
        for (String name : newClass2MethodNames.keySet()) {
            if (!newClass2MethodNames.get(name).isEmpty()) {
                isEmpty = false;
            }
        }
        if (isEmpty) {
            return;
        }
        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        ClassFileTransformer classFileTransformer = new ByteTransformer(targetClassNames, newClass2MethodNames);
        TransformerManager.getInstance(instrumentation).addTransformer(classFileTransformer);
        instrumentation.addTransformer(classFileTransformer, true);
        instrumentation.retransformClasses(targetClasses.toArray(new Class[0]));
    }
}

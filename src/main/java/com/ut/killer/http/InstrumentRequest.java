package com.ut.killer.http;

import com.ut.killer.classinfo.ClassUtils;
import com.ut.killer.parser.ClazzUtils;

import java.util.*;

public class InstrumentRequest {
    private MethodRequest methodRequest;

    private List<MethodRequest> mockMethods;

    public InstrumentRequest() {
    }

    public InstrumentRequest(MethodRequest methodRequest, List<MethodRequest> mockMethods) {
        this.methodRequest = methodRequest;
        this.mockMethods = mockMethods;
    }

    public List<MethodRequest> getMockMethods() {
        return mockMethods;
    }

    public void setMockMethods(List<MethodRequest> mockMethods) {
        this.mockMethods = mockMethods;
    }

    public MethodRequest getMethodRequest() {
        return methodRequest;
    }

    public void setMethodRequest(MethodRequest methodRequest) {
        this.methodRequest = methodRequest;
    }

    public Set<String> toClassNames() {
        Set<String> classNames = new HashSet<>();
        classNames.add(methodRequest.getClassName());
        classNames.addAll(mockMethods.stream().map(MethodRequest::getClassName).distinct().collect(java.util.stream.Collectors.toList()));
        return classNames;
    }

    public Map<String, Set<String>> toClass2Methods() {
        Map<String, Set<String>> class2Methods = new HashMap<>();
        String classNameByDot = methodRequest.getClassName();
        Set<String> implementClassNames = ClazzUtils.getImplementClassNames(classNameByDot);
        for (String implementClassName : implementClassNames) {
            String methodName = methodRequest.getMethodName();
            if (class2Methods.containsKey(implementClassName)) {
                class2Methods.get(implementClassName).add(methodName);
            } else {
                class2Methods.put(implementClassName, new HashSet<>(Collections.singletonList(methodName)));
            }
        }

        for (MethodRequest mockMethod : mockMethods) {
            String methodName = mockMethod.getMethodName();
            Set<String> implementMockClassNames = ClazzUtils.getImplementClassNames(mockMethod.getClassName());
            for (String implementMockClassName : implementMockClassNames) {
                if (class2Methods.containsKey(implementMockClassName)) {
                    class2Methods.get(implementMockClassName).add(methodName);
                } else {
                    class2Methods.put(implementMockClassName, new HashSet<>(Collections.singletonList(methodName)));
                }
            }
        }
        return class2Methods;
    }
}

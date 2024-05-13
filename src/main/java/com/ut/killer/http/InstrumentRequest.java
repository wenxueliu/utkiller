package com.ut.killer.http;

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

    public Map<String, List<String>> toClass2Methods() {
        Map<String, List<String>> class2Methods = new HashMap<>();
        if (class2Methods.containsKey(methodRequest.getClassName())) {
            class2Methods.get(methodRequest.getClassName()).add(methodRequest.getMethodName());
        } else {
            class2Methods.put(methodRequest.getClassName(), new ArrayList<>(Collections.singletonList(methodRequest.getMethodName())));
        }
        for (MethodRequest mockMethod : mockMethods) {
            if (class2Methods.containsKey(mockMethod.getClassName())) {
                class2Methods.get(mockMethod.getClassName()).add(mockMethod.getMethodName());
            } else {
                class2Methods.put(mockMethod.getClassName(), new ArrayList<>(Collections.singletonList(mockMethod.getMethodName())));
            }
        }
        return class2Methods;
    }
}

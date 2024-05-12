package com.ut.killer.http;

import java.util.List;

public class TreeRequest {
    private String mainClassPath;
    private MethodRequest methodRequest;

    private List<String> mockMethods;

    public String getMainClassPath() {
        return mainClassPath;
    }

    public void setMainClassPath(String mainClassPath) {
        this.mainClassPath = mainClassPath;
    }

    public MethodRequest getMethodRequest() {
        return methodRequest;
    }

    public void setMethodRequest(MethodRequest methodRequest) {
        this.methodRequest = methodRequest;
    }

    public List<String> getMockMethods() {
        return mockMethods;
    }

    public void setMockMethods(List<String> mockMethods) {
        this.mockMethods = mockMethods;
    }
}

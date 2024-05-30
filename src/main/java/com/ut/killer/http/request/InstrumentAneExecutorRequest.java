package com.ut.killer.http.request;

import java.util.List;

public class InstrumentAneExecutorRequest {
    private ExecRequest execRequest;

    private List<MethodRequest> mockRequests;

    public InstrumentAneExecutorRequest() {
    }

    public InstrumentAneExecutorRequest(ExecRequest execRequest, List<MethodRequest> mockRequests) {
        this.execRequest = execRequest;
        this.mockRequests = mockRequests;
    }

    public ExecRequest getExecRequest() {
        return execRequest;
    }

    public void setExecRequest(ExecRequest execRequest) {
        this.execRequest = execRequest;
    }

    public List<MethodRequest> getMockRequests() {
        return mockRequests;
    }

    public void setMockRequests(List<MethodRequest> mockRequests) {
        this.mockRequests = mockRequests;
    }

    public InstrumentRequest toInstrumentRequest() {
        MethodRequest methodRequest = execRequest.toMethodRequest();
        return new InstrumentRequest(methodRequest, mockRequests);
    }
}

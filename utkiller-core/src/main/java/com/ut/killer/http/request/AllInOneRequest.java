package com.ut.killer.http.request;

import java.util.HashSet;
import java.util.Set;

public class AllInOneRequest {
    private ExecRequest execRequest;

    private Set<String> includeClassPaths = new HashSet<>();

    private Set<String> excludeClassPaths = new HashSet<>();

    public AllInOneRequest() {
    }

    public ExecRequest getExecRequest() {
        return execRequest;
    }

    public void setExecRequest(ExecRequest execRequest) {
        this.execRequest = execRequest;
    }

    public Set<String> getIncludeClassPaths() {
        return includeClassPaths;
    }

    public void setIncludeClassPaths(Set<String> includeClassPaths) {
        this.includeClassPaths = includeClassPaths;
    }

    public Set<String> getExcludeClassPaths() {
        return excludeClassPaths;
    }

    public void setExcludeClassPaths(Set<String> excludeClassPaths) {
        this.excludeClassPaths = excludeClassPaths;
    }
}

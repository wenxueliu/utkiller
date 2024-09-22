package com.ut.killer.classinfo;

public abstract class ResultModel {
    private int jobId;

    public abstract String getType();

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}

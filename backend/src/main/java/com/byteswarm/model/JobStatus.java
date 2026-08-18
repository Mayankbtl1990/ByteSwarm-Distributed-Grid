package com.byteswarm.model;

public class JobStatus {
    private String jobId;
    private int totalChunks;
    private int completedChunks;
    private int failedChunks;
    private long startedAt;
    private long completedAt;
    private String state; // QUEUED, RUNNING, COMPLETED, FAILED

    public JobStatus() {}

    public JobStatus(String jobId, int totalChunks) {
        this.jobId = jobId;
        this.totalChunks = totalChunks;
        this.completedChunks = 0;
        this.failedChunks = 0;
        this.startedAt = System.currentTimeMillis();
        this.state = "RUNNING";
    }

    public double getProgress() {
        if (totalChunks == 0) return 0;
        return (completedChunks * 100.0) / totalChunks;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String s) { this.jobId = s; }
    public int getTotalChunks() { return totalChunks; }
    public void setTotalChunks(int i) { this.totalChunks = i; }
    public int getCompletedChunks() { return completedChunks; }
    public void setCompletedChunks(int i) { this.completedChunks = i; }
    public int getFailedChunks() { return failedChunks; }
    public void setFailedChunks(int i) { this.failedChunks = i; }
    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long l) { this.startedAt = l; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long l) { this.completedAt = l; }
    public String getState() { return state; }
    public void setState(String s) { this.state = s; }
}
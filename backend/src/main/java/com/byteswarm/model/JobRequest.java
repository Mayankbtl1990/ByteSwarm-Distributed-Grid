package com.byteswarm.model;

public class JobRequest {
    private String jobId;
    private int datasetSize;
    private int chunkSize;
    private String jobType;
    private long submittedAt;

    public JobRequest() {}

    public JobRequest(String jobId, int datasetSize, int chunkSize, String jobType) {
        this.jobId = jobId;
        this.datasetSize = datasetSize;
        this.chunkSize = chunkSize;
        this.jobType = jobType;
        this.submittedAt = System.currentTimeMillis();
    }

    public String getJobId() { return jobId; }
    public void setJobId(String s) { this.jobId = s; }
    public int getDatasetSize() { return datasetSize; }
    public void setDatasetSize(int i) { this.datasetSize = i; }
    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int i) { this.chunkSize = i; }
    public String getJobType() { return jobType; }
    public void setJobType(String s) { this.jobType = s; }
    public long getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(long l) { this.submittedAt = l; }
}
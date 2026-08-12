package com.byteswarm.chunking;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Chunk {
    private String chunkId;
    private String jobId;
    private List<String> payload;
    private String assignedWorkerId;
    private long timestamp;
    private ChunkStatus status;

    public Chunk() {}

    public Chunk(String chunkId, String jobId, List<String> payload) {
        this.chunkId = chunkId;
        this.jobId = jobId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
        this.status = ChunkStatus.PENDING;
    }

    public String getChunkId() { return chunkId; }
    public void setChunkId(String s) { this.chunkId = s; }
    public String getJobId() { return jobId; }
    public void setJobId(String s) { this.jobId = s; }
    public List<String> getPayload() { return payload; }
    public void setPayload(List<String> p) { this.payload = p; }
    public String getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(String s) { this.assignedWorkerId = s; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long t) { this.timestamp = t; }
    public ChunkStatus getStatus() { return status; }
    public void setStatus(ChunkStatus s) { this.status = s; }

    @Override
    public String toString() {
        return "Chunk{id=" + chunkId + ", job=" + jobId +
                ", size=" + (payload == null ? 0 : payload.size()) +
                ", worker=" + assignedWorkerId + ", status=" + status + "}";
    }
}
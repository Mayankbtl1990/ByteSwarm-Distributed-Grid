package com.byteswarm.model;

public class WorkerInfo {
    private String workerId;
    private long connectedAt;
    private int chunksProcessed;
    private boolean busy;

    public WorkerInfo() {}

    public WorkerInfo(String workerId) {
        this.workerId = workerId;
        this.connectedAt = System.currentTimeMillis();
        this.chunksProcessed = 0;
        this.busy = false;
    }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String s) { this.workerId = s; }
    public long getConnectedAt() { return connectedAt; }
    public void setConnectedAt(long l) { this.connectedAt = l; }
    public int getChunksProcessed() { return chunksProcessed; }
    public void setChunksProcessed(int i) { this.chunksProcessed = i; }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean b) { this.busy = b; }
    public void increment() { this.chunksProcessed++; }
}
package com.byteswarm.model;

import java.util.concurrent.atomic.AtomicLong;

public class WorkerInfo {
    private String workerId;
    private long connectedAt;
    private int chunksProcessed;
    private boolean busy;
    private final AtomicLong lastHeartbeat;

    public WorkerInfo() {
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
    }

    public WorkerInfo(String workerId) {
        this.workerId = workerId;
        this.connectedAt = System.currentTimeMillis();
        this.chunksProcessed = 0;
        this.busy = false;
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
    }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String s) { this.workerId = s; }
    public long getConnectedAt() { return connectedAt; }
    public void setConnectedAt(long l) { this.connectedAt = l; }
    public int getChunksProcessed() { return chunksProcessed; }
    public void setChunksProcessed(int i) { this.chunksProcessed = i; }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean b) { this.busy = b; }
    public long getLastHeartbeat() {
        return lastHeartbeat.get();
    }
    public void updateHeartbeat() {
        lastHeartbeat.set(System.currentTimeMillis());
    }
    public void increment() { this.chunksProcessed++; }
}
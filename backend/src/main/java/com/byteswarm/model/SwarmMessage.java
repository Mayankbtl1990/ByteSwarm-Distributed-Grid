package com.byteswarm.model;

public class SwarmMessage {
    private String type;
    private Object data;
    private long timestamp;

    public SwarmMessage() {}

    public SwarmMessage(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() { return type; }
    public void setType(String s) { this.type = s; }
    public Object getData() { return data; }
    public void setData(Object o) { this.data = o; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long t) { this.timestamp = t; }
}
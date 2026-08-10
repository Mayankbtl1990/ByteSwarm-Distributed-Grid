package com.byteswarm.util;

public final class Constants {
    private Constants() {}
    public static final String MSG_REGISTERED = "REGISTERED";
    public static final String MSG_COMPUTE_CHUNK = "COMPUTE_CHUNK";
    public static final String MSG_CHUNK_RESULT = "CHUNK_RESULT";
    public static final String MSG_HEARTBEAT = "HEARTBEAT";
    public static final String MSG_ERROR = "ERROR";

    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_PATH = "/swarm";
}
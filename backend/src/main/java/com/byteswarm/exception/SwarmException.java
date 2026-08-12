package com.byteswarm.exception;

public class SwarmException extends RuntimeException {
    public SwarmException(String message) { super(message); }
    public SwarmException(String message, Throwable cause) { super(message, cause); }
}
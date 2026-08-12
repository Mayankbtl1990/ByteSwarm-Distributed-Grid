package com.byteswarm.exception;

public class NoWorkersAvailableException extends SwarmException {
    public NoWorkersAvailableException() {
        super("No worker nodes are currently connected to the swarm.");
    }
}
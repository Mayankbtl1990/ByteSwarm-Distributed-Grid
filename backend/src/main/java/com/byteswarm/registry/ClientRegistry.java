package com.byteswarm.registry;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    private static final Logger log = LoggerFactory.getLogger(ClientRegistry.class);
    private static final ClientRegistry INSTANCE = new ClientRegistry();

    private final Map<String, Channel> workers = new ConcurrentHashMap<>();

    private ClientRegistry() {}

    public static ClientRegistry getInstance() {
        return INSTANCE;
    }

    public void register(String workerId, Channel channel) {
        workers.put(workerId, channel);
        log.info("Registered worker {} | Total: {}", workerId, workers.size());
    }

    public void unregister(String workerId) {
        workers.remove(workerId);
        log.info("Removed worker {} | Total: {}", workerId, workers.size());
    }

    public Channel get(String workerId) {
        return workers.get(workerId);
    }

    public Map<String, Channel> getAllWorkers() {
        return Collections.unmodifiableMap(workers);
    }

    public int size() {
        return workers.size();
    }
}

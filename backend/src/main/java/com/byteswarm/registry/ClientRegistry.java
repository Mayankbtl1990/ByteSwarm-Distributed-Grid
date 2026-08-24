package com.byteswarm.registry;

import com.byteswarm.model.WorkerInfo;
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
    private final Map<String, WorkerInfo> workerInfo = new ConcurrentHashMap<>();

    private ClientRegistry() {}

    public static ClientRegistry getInstance() { return INSTANCE; }

    public void register(String workerId, Channel channel) {
        workers.put(workerId, channel);
        workerInfo.put(workerId, new WorkerInfo(workerId));
        log.info(" Registered {} | Total: {}", workerId, workers.size());
    }

    public void unregister(String workerId) {
        workers.remove(workerId);
        workerInfo.remove(workerId);
        log.info(" Removed {} | Total: {}", workerId, workers.size());
    }

    public Channel get(String workerId) { return workers.get(workerId); }
    public WorkerInfo getInfo(String workerId) { return workerInfo.get(workerId); }

    public void markBusy(String workerId, boolean busy) {
        WorkerInfo info = workerInfo.get(workerId);
        if (info != null) info.setBusy(busy);
    }

    public void incrementChunks(String workerId) {
        WorkerInfo info = workerInfo.get(workerId);
        if (info != null) info.increment();
    }

    public Map<String, Channel> getAllWorkers() {
        return Collections.unmodifiableMap(workers);
    }

    public Map<String, WorkerInfo> getAllWorkerInfo() {
        return Collections.unmodifiableMap(workerInfo);
    }

    public int size() { return workers.size(); }
}
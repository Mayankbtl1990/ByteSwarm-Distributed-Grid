package com.byteswarm.server;

import com.byteswarm.model.WorkerInfo;
import com.byteswarm.registry.ClientRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HeartbeatManager {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatManager.class);
    private static final long CHECK_INTERVAL_MS = 3_000L;
    private static final long HEARTBEAT_TIMEOUT_MS = 10_000L;

    private final ClientRegistry registry;
    private final Consumer<WorkerInfo> onWorkerDropped;
    private final ScheduledExecutorService scheduler;

    public HeartbeatManager(ClientRegistry registry, Consumer<WorkerInfo> onWorkerDropped) {
        this.registry = registry;
        this.onWorkerDropped = onWorkerDropped;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-monitor");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkHeartbeats,
                CHECK_INTERVAL_MS, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        log.info("HeartbeatManager started (check every {}ms, timeout {}ms)",
                CHECK_INTERVAL_MS, HEARTBEAT_TIMEOUT_MS);
    }

    public void stop() {
        scheduler.shutdownNow();
        log.info("HeartbeatManager stopped");
    }

    private void checkHeartbeats() {
        try {
            long now = System.currentTimeMillis();
            List<WorkerInfo> all = new ArrayList<>(registry.getAllWorkerInfo().values());
            for (WorkerInfo w : all) {
                long silentFor = now - w.getLastHeartbeat();
                if (silentFor > HEARTBEAT_TIMEOUT_MS) {
                    WorkerInfo removed = registry.getAllWorkerInfo().remove(w.getWorkerId());
                    if(removed!=null){
                        registry.getAllWorkers().remove(w.getWorkerId());
                        log.warn(" Worker {} silent for {}ms — marking dropped",
                            w.getWorkerId(), silentFor);
                        onWorkerDropped.accept(w);
                    }
                }   
            }
        } catch (Exception e) {
            log.error("Heartbeat check failed", e);
        }
    }
}
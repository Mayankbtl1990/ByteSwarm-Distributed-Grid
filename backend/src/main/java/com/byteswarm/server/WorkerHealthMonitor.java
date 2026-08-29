package com.byteswarm.server;

import com.byteswarm.chunking.JobManager;
import com.byteswarm.registry.ClientRegistry;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WorkerHealthMonitor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(WorkerHealthMonitor.class);

    private final long checkIntervalMs;
    private final long heartbeatTimeoutMs;
    private volatile boolean running = true;

    public WorkerHealthMonitor(long checkIntervalMs, long heartbeatTimeoutMs) {
        this.checkIntervalMs = checkIntervalMs;
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }

    @Override
    public void run() {
        log.info("WorkerHealthMonitor started | interval={}ms timeout={}ms",
                checkIntervalMs, heartbeatTimeoutMs);

        while (running) {
            try {
                List<String> staleWorkers = ClientRegistry.getInstance().findStaleWorkers(heartbeatTimeoutMs);

                for (String workerId : staleWorkers) {
                    if (!ClientRegistry.getInstance().contains(workerId)) {
                        continue;
                    }

                    log.warn("Stale worker detected: {}", workerId);

                    JobManager.getInstance().incrementDroppedWorkers();

                    try {
                        ChunkDispatcher.handleWorkerDropped(workerId);
                    } catch (Exception e) {
                        log.warn("Chunk recovery failed for stale worker {}: {}", workerId, e.getMessage());
                    }

                    Channel ch = ClientRegistry.getInstance().get(workerId);
                    if (ch != null && ch.isOpen()) {
                        ch.close();
                    }

                    ClientRegistry.getInstance().unregister(workerId);
                }

                Thread.sleep(checkIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                log.error("WorkerHealthMonitor error: {}", e.getMessage(), e);
            }
        }

        log.info("WorkerHealthMonitor stopped.");
    }

    public void shutdown() {
        running = false;
    }
}
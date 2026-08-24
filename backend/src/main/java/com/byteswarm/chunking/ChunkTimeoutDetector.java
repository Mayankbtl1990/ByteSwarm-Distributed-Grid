package com.byteswarm.chunking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChunkTimeoutDetector {
    private static final Logger log = LoggerFactory.getLogger(ChunkTimeoutDetector.class);
    private static final long CHECK_INTERVAL_MS = 2_000L;
    private static final long CHUNK_TIMEOUT_MS = 15_000L;
    private final JobManager jobManager;
    private final Consumer<Chunk> onChunkTimeout;
    private final ScheduledExecutorService scheduler;

    public ChunkTimeoutDetector(JobManager jobManager, Consumer<Chunk> onChunkTimeout) {
        this.jobManager = jobManager;
        this.onChunkTimeout = onChunkTimeout;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chunk-timeout-detector");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::scan, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        log.info("ChunkTimeoutDetector started (timeout {}ms)", CHUNK_TIMEOUT_MS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void scan() {
        try {
            long now = System.currentTimeMillis();
            List<Chunk> stuck = jobManager.getInFlightChunks().stream()
                    .filter(c -> c.getStatus() == ChunkStatus.DISPATCHED)
                    .filter(c -> now - c.getDispatchedAt() > CHUNK_TIMEOUT_MS).collect(Collectors.toList());
            for (Chunk c : stuck) {
                log.warn(" Chunk {} timed out (dispatched {}ms ago) on worker {}", c.getChunkId(),
                        now - c.getDispatchedAt(), c.getAssignedWorkerId());
                onChunkTimeout.accept(c);
            }
        } catch (Exception e) {
            log.error("Timeout scan failed", e);
        }
    }
}
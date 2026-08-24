package com.byteswarm;

import com.byteswarm.chunking.ChunkingEngine;
import com.byteswarm.chunking.ChunkTimeoutDetector;
import com.byteswarm.chunking.JobManager;
import com.byteswarm.server.*;
import com.byteswarm.config.AppConfig;
import com.byteswarm.registry.ClientRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BackendApplication {
    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) throws Exception {
        System.out.println("   ByteSwarm Server Starting...    ");

        int port = AppConfig.getInt("server.port", 8080);
        ClientRegistry registry = ClientRegistry.getInstance();
        JobManager jobManager = JobManager.getInstance();
        
        // Heartbeat Monitor Setup
        HeartbeatManager heartbeat = new HeartbeatManager(registry, dropped -> {
            log.warn("Worker {} dropped — dispatcher will re-queue its chunks", dropped.getWorkerId());
            ChunkDispatcher.handleWorkerDropped(dropped.getWorkerId());
        });
        heartbeat.start();
        Runtime.getRuntime().addShutdownHook(new Thread(heartbeat::stop));

        // Timeout Detector Wiring
        ChunkTimeoutDetector timeoutDetector = new ChunkTimeoutDetector(jobManager, chunk -> {
            log.warn("Chunk {} timeout — will re-dispatch", chunk.getChunkId());
            // Re-dispatch logic call if needed: ChunkDispatcher.dispatchSingle(chunk);
        });
        timeoutDetector.start();
        Runtime.getRuntime().addShutdownHook(new Thread(timeoutDetector::stop));

        MetricsHttpServer.start(8081);
        new Thread(BackendApplication::runDemoJob, "job-scheduler").start();

        new NettyWebSocketServer(port).start();
    }

    private static void runDemoJob() {
        try {
            Thread.sleep(15000);
            while (true) {
                if (ClientRegistry.getInstance().size() > 0) {
                    log.info(" Auto-submitting demo job...");
                    List<String> dataset = ChunkingEngine.generateMockDataset(10_000);
                    JobManager.getInstance().submitJob(dataset, 1000);
                }
                Thread.sleep(60000);
            }
        } catch (Exception e) {
            log.error("Scheduler stopped: {}", e.getMessage());
        }
    }
}
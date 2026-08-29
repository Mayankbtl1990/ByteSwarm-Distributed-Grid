package com.byteswarm;

import com.byteswarm.chunking.ChunkingEngine;
import com.byteswarm.chunking.JobManager;
import com.byteswarm.config.AppConfig;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.server.MetricsHttpServer;
import com.byteswarm.server.NettyWebSocketServer;
import com.byteswarm.server.WorkerHealthMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BackendApplication {
    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) throws Exception {
        System.out.println("   ByteSwarm Server Starting...    ");

        AppConfig.load();

        int port = AppConfig.getInt("server.port", 8080);
        int metricsPort = AppConfig.getInt("metrics.port", 8081);

        log.info(" ByteSwarm config loaded | wsPort={} metricsPort={}", port, metricsPort);

        ClientRegistry.getInstance();
        JobManager.getInstance();

        log.info(" Starting Metrics HTTP server...");
        MetricsHttpServer.start(metricsPort);

        log.info(" Starting worker health monitor thread...");
        Thread healthMonitorThread = new Thread(
                new WorkerHealthMonitor(5000, 15000),
                "worker-health-monitor"
        );
        healthMonitorThread.setDaemon(true);
        healthMonitorThread.start();

        log.info(" Starting demo job scheduler thread...");
        Thread schedulerThread = new Thread(BackendApplication::runDemoJob, "job-scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        log.info(" Starting Netty WebSocket server on port {}...", port);
        new NettyWebSocketServer(port).start();
    }

    private static void runDemoJob() {
        try {
            Thread.sleep(15000);
            while (true) {
                if (ClientRegistry.getInstance().size() > 0) {
                    log.info("Auto-submitting demo job...");
                    List<String> dataset = ChunkingEngine.generateMockDataset(10_000);
                    String jobId = JobManager.getInstance().submitJob(dataset, 1000);
                    log.info(" Demo job submitted successfully: {}", jobId);
                } else {
                    log.info(" No workers available. Skipping demo job submission cycle.");
                }
                Thread.sleep(60000);
            }
        } catch (Exception e) {
            log.error("Scheduler stopped: {}", e.getMessage(), e);
        }
    }
}
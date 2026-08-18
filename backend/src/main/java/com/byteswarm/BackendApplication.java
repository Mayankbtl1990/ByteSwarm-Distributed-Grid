package com.byteswarm;

import com.byteswarm.chunking.ChunkingEngine;
import com.byteswarm.chunking.JobManager;
import com.byteswarm.config.AppConfig;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.server.NettyWebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BackendApplication {
    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     ByteSwarm Server Starting...     ║");
        System.out.println("╚══════════════════════════════════════╝");
        
        int port = AppConfig.getInt("server.port", 8080);
        
        // Start auto-demo job in background thread
        new Thread(BackendApplication::runDemoJob, "job-scheduler").start();

        // Start WebSocket Server
        new NettyWebSocketServer(port).start();
    }

    private static void runDemoJob() {
        try {
            Thread.sleep(15000); // Wait 15s for workers to connect
            while (true) {
                if (ClientRegistry.getInstance().size() > 0) {
                    log.info(" Auto-submitting demo job...");
                    List<String> dataset = ChunkingEngine.generateMockDataset(10_000);
                    String jobId = JobManager.getInstance().submitJob(dataset, 1000);
                    log.info(" Job {} submitted. Waiting 60s before next...", jobId);
                } else {
                    log.info(" No workers connected. Skipping demo job.");
                }
                Thread.sleep(60000);
            }
        } catch (Exception e) {
            log.error("Demo job scheduler stopped: {}", e.getMessage());
        }
    }
}
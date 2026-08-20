package com.byteswarm;

import com.byteswarm.chunking.ChunkingEngine;
import com.byteswarm.chunking.JobManager;
import com.byteswarm.registry.ClientRegistry;

import java.util.List;

public class LoadTestRunner {

    public static void main(String[] args) throws InterruptedException {
        int minWorkers = 3;
        System.out.println("⏳ Waiting for at least " + minWorkers + " workers to connect...");

        while (ClientRegistry.getInstance().size() < minWorkers) {
            Thread.sleep(1000);
            System.out.print(".");
        }
        System.out.println("\n✅ " + ClientRegistry.getInstance().size() + " workers connected");

        System.out.println("🎯 Generating 5,000,000 mock equations...");
        List<String> dataset = ChunkingEngine.generateMockDataset(5_000_000);

        long start = System.currentTimeMillis();
        String jobId = JobManager.getInstance().submitJob(dataset, 1000);
        System.out.println("📋 Job submitted: " + jobId);

        while (!"COMPLETED".equals(JobManager.getInstance().getStatus(jobId).getState())) {
            Thread.sleep(1000);
            var status = JobManager.getInstance().getStatus(jobId);
            System.out.printf("  Progress: %d/%d chunks (%.1f%%)%n",
                    status.getCompletedChunks(), status.getTotalChunks(), status.getProgress());
        }

        long elapsed = System.currentTimeMillis() - start;
        double avg = JobManager.getInstance().getAverageComputeTime(jobId);
        System.out.println("         LOAD TEST RESULTS          ");
        System.out.printf("Total time:       %d ms%n", elapsed);
        System.out.printf("Chunks:           %d%n", JobManager.getInstance().getStatus(jobId).getTotalChunks());
        System.out.printf("Avg per chunk:    %.2f ms%n", avg);
        System.out.printf("Workers used:     %d%n", ClientRegistry.getInstance().size());
    }
}
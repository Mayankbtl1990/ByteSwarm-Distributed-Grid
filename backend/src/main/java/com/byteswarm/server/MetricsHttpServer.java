package com.byteswarm.server;

import com.byteswarm.chunking.JobManager;
import com.byteswarm.model.JobStatus;
import com.byteswarm.model.WorkerInfo;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.util.JsonUtil;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MetricsHttpServer {
    private static final Logger log = LoggerFactory.getLogger(MetricsHttpServer.class);

    public static void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/metrics", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            long now = System.currentTimeMillis();
            List<Map<String, Object>> nodes = new ArrayList<>();

            for (Map.Entry<String, WorkerInfo> entry : ClientRegistry.getInstance().getAllWorkerInfo().entrySet()) {
                String id = entry.getKey();
                WorkerInfo info = entry.getValue();
                long heartbeatAgeMs = now - info.getLastHeartbeat();

                String state;
                if (heartbeatAgeMs > 15000) {
                    state = "STALE";
                } else if (info.isBusy()) {
                    state = "BUSY";
                } else {
                    state = "IDLE";
                }

                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", id);
                node.put("busy", info.isBusy());
                node.put("chunksProcessed", info.getChunksProcessed());
                node.put("connectedAt", info.getConnectedAt());
                node.put("lastHeartbeat", info.getLastHeartbeat());
                node.put("heartbeatAgeMs", heartbeatAgeMs);
                node.put("state", state);

                nodes.add(node);
            }

            List<JobStatus> jobs = new ArrayList<>(JobManager.getInstance().getAllJobs());

            int totalJobs = jobs.size();
            long completedJobs = jobs.stream()
                    .filter(job -> "COMPLETED".equalsIgnoreCase(job.getState()))
                    .count();
            long runningJobs = jobs.stream()
                    .filter(job -> !"COMPLETED".equalsIgnoreCase(job.getState()))
                    .count();

            long busyWorkers = nodes.stream()
                    .filter(node -> "BUSY".equals(node.get("state")))
                    .count();
            long staleWorkers = nodes.stream()
                    .filter(node -> "STALE".equals(node.get("state")))
                    .count();

            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("activeWorkers", nodes.size());
            metrics.put("busyWorkers", busyWorkers);
            metrics.put("idleWorkers", Math.max(0, nodes.size() - busyWorkers - staleWorkers));
            metrics.put("staleWorkers", staleWorkers);
            metrics.put("totalJobs", totalJobs);
            metrics.put("completedJobs", completedJobs);
            metrics.put("runningJobs", runningJobs);
            metrics.put("totalChunks", JobManager.getInstance().getTotalChunksCount());
            metrics.put("completedChunks", JobManager.getInstance().getCompletedChunksCount());
            metrics.put("reassignedChunks", JobManager.getInstance().getReassignedChunksCount());
            metrics.put("droppedWorkers", JobManager.getInstance().getDroppedWorkersCount());
            metrics.put("timestamp", now);

            List<Map<String, Object>> jobSummaries = JobManager.getInstance().getAllJobSummaries();
            List<Map<String, Object>> completedJobOutputs = jobSummaries.stream()
                    .filter(summary -> "COMPLETED".equalsIgnoreCase(String.valueOf(summary.get("state"))))
                    .toList();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("nodes", nodes);
            payload.put("metrics", metrics);
            payload.put("jobs", jobs);
            payload.put("jobSummaries", jobSummaries);
            payload.put("completedJobSummaries", completedJobOutputs);

            byte[] response = JsonUtil.toJson(payload).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.setExecutor(null);
        server.start();
        log.info("Metrics HTTP server started on http://localhost:{}/api/metrics", port);
    }
}
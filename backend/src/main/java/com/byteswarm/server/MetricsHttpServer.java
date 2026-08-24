package com.byteswarm.server;

import com.byteswarm.chunking.JobManager;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.util.JsonUtil;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class MetricsHttpServer {
    private static final Logger log = LoggerFactory.getLogger(MetricsHttpServer.class);

    public static void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/metrics", exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            List<Map<String, Object>> nodes = new ArrayList<>();
            ClientRegistry.getInstance().getAllWorkerInfo().forEach((id, info) ->
            nodes.add(Map.of(
                "id", id,
                "busy", info.isBusy(),
                "chunksProcessed", info.getChunksProcessed(),
                "connectedAt", info.getConnectedAt()
            )));
            Map<String, Object> payload = Map.of(
                    "nodes", nodes,
                    "metrics", Map.of(
                            "activeWorkers", nodes.size(),
                            "totalJobs", JobManager.getInstance().getAllJobs().size(),
                            "timestamp", System.currentTimeMillis()
                    ),
                    "jobs", JobManager.getInstance().getAllJobs()
            );

            byte[] response = JsonUtil.toJson(payload).getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.setExecutor(null);
        server.start();
        log.info(" Metrics HTTP server started on http://localhost:{}/api/metrics", port);
    }
}
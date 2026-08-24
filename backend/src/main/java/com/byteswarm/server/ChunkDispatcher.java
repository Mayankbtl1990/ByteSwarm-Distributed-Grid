package com.byteswarm.server;

import com.byteswarm.chunking.Chunk;
import com.byteswarm.chunking.ChunkStatus;
import com.byteswarm.exception.NoWorkersAvailableException;
import com.byteswarm.model.SwarmMessage;
import com.byteswarm.model.WorkerInfo;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ChunkDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ChunkDispatcher.class);

    private ChunkDispatcher() {}

    public static void dispatch(List<Chunk> chunks) {
        Map<String, Channel> workers = ClientRegistry.getInstance().getAllWorkers();
        if (workers.isEmpty()) throw new NoWorkersAvailableException();

        Map<String, WorkerInfo>workerInfo = ClientRegistry.getInstance().getAllWorkerInfo();
        log.info(" Smart-dispatching {} chunks across {} workers", chunks.size(), workers.size());

        int dispatched = 0;
        for (Chunk chunk : chunks) {
            String bestWorkerId = pickLeastLoadedWorker(workers, workerInfo);
            if (bestWorkerId == null) {
                log.warn("No available worker for chunk {}", chunk.getChunkId());
                continue;
            }

            Channel target = workers.get(bestWorkerId);
            chunk.setAssignedWorkerId(bestWorkerId);
            chunk.setStatus(ChunkStatus.DISPATCHED);

            SwarmMessage msg = new SwarmMessage("COMPUTE_CHUNK", chunk);
            target.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msg)));

            ClientRegistry.getInstance().markBusy(bestWorkerId, true);
            dispatched++;
        }

        log.info(" Dispatched {} / {} chunks", dispatched, chunks.size());
    }

    private static String pickLeastLoadedWorker(
            Map<String, Channel> workers,
            Map<String, WorkerInfo>workerInfo) {

            List<Map.Entry<String, Channel>>activeWorkers = workers.entrySet().stream()
            .filter(e ->e.getValue().isActive() &&e.getValue().isWritable())
            .collect(Collectors.toList());

            if (activeWorkers.isEmpty()) return null;
                return activeWorkers.stream()
                .min(Comparator
                .comparingInt((Map.Entry<String, Channel> e) -> {
                WorkerInfo info = workerInfo.get(e.getKey());
                            return info == null ?0 :info.getChunksProcessed();
                        })
                .thenComparing(e -> {
                WorkerInfo info = workerInfo.get(e.getKey());
                            return info != null &&info.isBusy() ?1 : 0;
                        }))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static void dispatchToWorker(Chunk chunk, String workerId) {
        Channel ch = ClientRegistry.getInstance().get(workerId);
        if (ch == null || !ch.isActive()) return;

            chunk.setAssignedWorkerId(workerId);
            chunk.setStatus(ChunkStatus.DISPATCHED);
            SwarmMessage msg = new SwarmMessage("COMPUTE_CHUNK", chunk);
            ch.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msg)));
            ClientRegistry.getInstance().markBusy(workerId, true);
    }
    public static void handleWorkerDropped(String workerId) {
        log.info("[stub] handleWorkerDropped({})", workerId);
    }
}

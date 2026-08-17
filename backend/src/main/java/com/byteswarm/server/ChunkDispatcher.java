package com.byteswarm.server;

import com.byteswarm.chunking.Chunk;
import com.byteswarm.chunking.ChunkStatus;
import com.byteswarm.exception.NoWorkersAvailableException;
import com.byteswarm.model.SwarmMessage;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChunkDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ChunkDispatcher.class);

    private ChunkDispatcher() {}

    public static void dispatch(List<Chunk> chunks) {
        Map<String, Channel> workers = ClientRegistry.getInstance().getAllWorkers();
        if (workers.isEmpty()) {
            throw new NoWorkersAvailableException();
        }

        List<Channel> workerList = new ArrayList<>(workers.values());
        int workerCount = workerList.size();
        log.info(" Dispatching {} chunks across {} workers", chunks.size(), workerCount);

        int i = 0;
        int dispatched = 0;
        for (Chunk chunk : chunks) {
            Channel target = workerList.get(i % workerCount);
            if (!target.isActive()) {
                i++;
                continue;
            }

            chunk.setAssignedWorkerId(target.id().asShortText());
            chunk.setStatus(ChunkStatus.DISPATCHED);

            SwarmMessage msg = new SwarmMessage("COMPUTE_CHUNK", chunk);
            String json = JsonUtil.toJson(msg);
            target.writeAndFlush(new TextWebSocketFrame(json));

            dispatched++;
            i++;
        }

        log.info(" Successfully dispatched {} / {} chunks", dispatched, chunks.size());
    }

    public static void dispatchToWorker(Chunk chunk, String workerId) {
        Channel ch = ClientRegistry.getInstance().get(workerId);
        if (ch == null || !ch.isActive()) {
            log.warn("Worker {} not available for chunk {}", workerId, chunk.getChunkId());
            return;
        }

        chunk.setAssignedWorkerId(workerId);
        chunk.setStatus(ChunkStatus.DISPATCHED);

        SwarmMessage msg = new SwarmMessage("COMPUTE_CHUNK", chunk);
        ch.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msg)));
    }
}
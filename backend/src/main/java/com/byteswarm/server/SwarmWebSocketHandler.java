package com.byteswarm.server;

import com.byteswarm.chunking.JobManager;
import com.byteswarm.registry.ClientRegistry;
import com.byteswarm.util.JsonUtil;
import com.byteswarm.model.SwarmMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SwarmWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(SwarmWebSocketHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String workerId = ctx.channel().id().asShortText();
        ClientRegistry.getInstance().register(workerId, ctx.channel());
        log.info(" Worker CONNECTED: {} | Total: {}",
                workerId, ClientRegistry.getInstance().size());

        SwarmMessage welcome = new SwarmMessage(
                "REGISTERED",
                Map.of(
                        "workerId", workerId,
                        "serverTime", System.currentTimeMillis()
                )
        );
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(welcome)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String workerId = ctx.channel().id().asShortText();
        try {
            ChunkDispatcher.handleWorkerDropped(workerId);
        } catch (Exception e) {
            log.warn(" Recovery failed for dropped worker {}: {}", workerId, e.getMessage());
        } finally {
            ClientRegistry.getInstance().unregister(workerId);
            log.info(" Worker DISCONNECTED: {}", workerId);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String workerId = ctx.channel().id().asShortText();
        try {
            SwarmMessage msg = JsonUtil.fromJson(frame.text(), SwarmMessage.class);
            handleMessage(ctx, workerId, msg);
        } catch (Exception e) {
            log.warn(" Bad message from {}: {}", workerId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(ChannelHandlerContext ctx, String workerId, SwarmMessage msg) {
        if (msg == null || msg.getType() == null) {
            log.warn(" Null or malformed message from {}", workerId);
            return;
        }

        switch (msg.getType()) {
            case "REGISTER" -> log.info(" {} capabilities: {}", workerId, msg.getData());

            case "HEARTBEAT" -> {
                ClientRegistry.getInstance().heartbeat(workerId);
                log.debug(" Heartbeat updated for {}", workerId);
            }

            case "CHUNK_RESULT" -> {
                Map<String, Object> data = (Map<String, Object>) msg.getData();
                if (data == null) {
                    log.warn(" Missing CHUNK_RESULT payload from {}", workerId);
                    return;
                }

                String chunkId = (String) data.get("chunkId");
                String jobId = (String) data.get("jobId");
                Object results = data.get("results");
                Number computeTime = (Number) data.get("computeTimeMs");
                long ms = computeTime != null ? computeTime.longValue() : 0;

                if (chunkId == null || jobId == null) {
                    log.warn(" Invalid CHUNK_RESULT from {}: missing jobId/chunkId", workerId);
                    return;
                }

                log.info(" Result from {} — chunk {} in {}ms", workerId, chunkId, ms);
                JobManager.getInstance().recordResult(jobId, chunkId, results, ms);
                ClientRegistry.getInstance().incrementChunks(workerId);
                ClientRegistry.getInstance().markBusy(workerId, false);
            }

            case "BUSY_STATUS" -> {
                Map<String, Object> data = (Map<String, Object>) msg.getData();
                boolean busy = data != null && Boolean.TRUE.equals(data.get("busy"));
                ClientRegistry.getInstance().markBusy(workerId, busy);
                log.debug(" {} is now {}", workerId, busy ? "BUSY" : "IDLE");
            }

            default -> log.warn(" Unknown type: {}", msg.getType());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(" Error: {}", cause.getMessage(), cause);
        ctx.close();
    }
}
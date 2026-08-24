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

        SwarmMessage welcome = new SwarmMessage("REGISTERED",
                Map.of("workerId", workerId, "serverTime", System.currentTimeMillis()));
        ctx.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(welcome)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String workerId = ctx.channel().id().asShortText();
        ClientRegistry.getInstance().unregister(workerId);
        log.info(" Worker DISCONNECTED: {}", workerId);
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
        switch (msg.getType()) {
            case "REGISTER" -> log.info(" {} capabilities: {}", workerId, msg.getData());
            case "HEARTBEAT" -> log.debug(" Heartbeat from {}", workerId);
            case "CHUNK_RESULT" -> {
            Map<String, Object> data = (Map<String, Object>) msg.getData();
            String chunkId = (String) data.get("chunkId");
            String jobId = (String) data.get("jobId");
            Object results = data.get("results");
            Number computeTime = (Number) data.get("computeTimeMs");
            long ms = computeTime != null ? computeTime.longValue() : 0;

            log.info(" Result from {} — chunk {} in {}ms", workerId, chunkId, ms);
            JobManager.getInstance().recordResult(jobId, chunkId, results, ms);
            ClientRegistry.getInstance().incrementChunks(workerId);
            ClientRegistry.getInstance().markBusy(workerId, false);
            }
            case "BUSY_STATUS" -> {
            Map<String, Object> data = (Map<String, Object>) msg.getData();
            boolean busy = Boolean.TRUE.equals(data.get("busy"));
            ClientRegistry.getInstance().markBusy(workerId, busy);
            log.debug(" {} is now {}", workerId, busy ? "BUSY" : "IDLE");
            }
            default -> log.warn(" Unknown type: {}", msg.getType());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(" Error: {}", cause.getMessage());
        ctx.close();
    }
}
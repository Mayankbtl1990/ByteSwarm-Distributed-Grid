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
        log.info(" Worker DISCONNECTED: {} | Remaining: {}",
                workerId, ClientRegistry.getInstance().size());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String workerId = ctx.channel().id().asShortText();
        String raw = frame.text();
        log.info(" From {}: {}", workerId, raw);

        try {
            SwarmMessage msg = JsonUtil.fromJson(raw, SwarmMessage.class);
            handleMessage(ctx, workerId, msg);
        } catch (Exception e) {
            log.warn(" Bad message from {}: {}", workerId, e.getMessage());
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, String workerId, SwarmMessage msg) {
        switch (msg.getType()) {
            case "REGISTER" -> log.info("REGISTER capabilities from {}: {}", workerId, msg.getData());
            case "HEARTBEAT" -> log.debug(" Heartbeat from {}", workerId);
            case "CHUNK_RESULT" -> log.info(" Chunk result from {} (handled in Week 2)", workerId);
            default -> log.warn(" Unknown message type: {}", msg.getType());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(" Error: {}", cause.getMessage());
        ctx.close();
    }
}
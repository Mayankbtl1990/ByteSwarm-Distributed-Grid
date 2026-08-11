package com.byteswarm.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwarmWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(SwarmWebSocketHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String workerId = ctx.channel().id().asShortText();
        log.info(" Worker CONNECTED: {}", workerId);

        String welcome = String.format(
                "{\"type\":\"REGISTERED\",\"workerId\":\"%s\"}", workerId);
        ctx.writeAndFlush(new TextWebSocketFrame(welcome));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String workerId = ctx.channel().id().asShortText();
        log.info(" Worker DISCONNECTED: {}", workerId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String workerId = ctx.channel().id().asShortText();
        log.info(" From {}: {}", workerId, frame.text());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(" Error: {}", cause.getMessage());
        ctx.close();
    }
}
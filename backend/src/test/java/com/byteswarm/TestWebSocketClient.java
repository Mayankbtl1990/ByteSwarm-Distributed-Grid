package com.byteswarm;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.net.URI;

/**
 * Simulates multiple browser tabs connecting as workers.
 * Run: mvn test-compile exec:java -Dexec.classpathScope=test
 *      -Dexec.mainClass=com.byteswarm.TestWebSocketClient
 */
public class TestWebSocketClient {

    public static void main(String[] args) throws Exception {
        int numClients = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        System.out.println("🧪 Starting " + numClients + " test worker clients...");

        for (int i = 0; i < numClients; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    connectClient(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            Thread.sleep(300);
        }

        Thread.sleep(60000);
        System.exit(0);
    }

    private static void connectClient(int id) throws Exception {
        URI uri = new URI("ws://localhost:8080/swarm");
        EventLoopGroup group = new NioEventLoopGroup();

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(8192));
                        p.addLast(new SimpleChannelInboundHandler<Object>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                                if (msg instanceof FullHttpResponse resp) {
                                    handshaker.finishHandshake(ctx.channel(), resp);
                                    System.out.println("[Client " + id + "] ✅ Connected");
                                    ctx.channel().writeAndFlush(new TextWebSocketFrame(
                                            "{\"type\":\"REGISTER\",\"data\":{\"cores\":4}}"));
                                } else if (msg instanceof TextWebSocketFrame frame) {
                                    System.out.println("[Client " + id + "] 📨 " + frame.text());
                                }
                            }
                        });
                    }
                });

        Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handshaker.handshake(ch);
    }
}
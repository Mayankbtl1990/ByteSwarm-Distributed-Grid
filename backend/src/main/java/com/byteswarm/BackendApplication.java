package com.byteswarm;

import com.byteswarm.config.AppConfig;
import com.byteswarm.server.NettyWebSocketServer;

public class BackendApplication {

	public static void main(String[] args) throws Exception {
		 System.out.println("╔══════════════════════════════════════╗");
	     System.out.println("║      ByteSwarm Server Starting...    ║");
	     System.out.println("╚══════════════════════════════════════╝");
	     int port = AppConfig.getInt("server.port", 8080);
	     new NettyWebSocketServer(port).start();
	}

}

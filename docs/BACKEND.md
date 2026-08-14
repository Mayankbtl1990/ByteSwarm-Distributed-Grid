# ByteSwarm Backend Documentation

## Architecture
┌──────────────────────────────────────┐
│ ByteSwarmApplication (main) │
└─────────────────┬────────────────────┘
│
┌─────────▼──────────┐
│ NettyWebSocketServer│
│ (port 8080) │
└─────────┬───────────┘
│
┌─────────▼───────────┐
│ SwarmWebSocketHandler│
└──────┬──────────────┘
│
┌────────▼─────────┐
│ ClientRegistry │
│ (Singleton) │
└──────────────────┘

## Package Layout

| Package | Purpose |
|---------|---------|
| `com.byteswarm` | Main entry point |
| `com.byteswarm.server` | Netty server + handlers |
| `com.byteswarm.registry` | Connected workers tracking |
| `com.byteswarm.chunking` | Chunk data models |
| `com.byteswarm.model` | Shared DTOs (SwarmMessage, WorkerInfo) |
| `com.byteswarm.config` | AppConfig loader |
| `com.byteswarm.util` | JsonUtil, Constants |
| `com.byteswarm.exception` | Custom exceptions |

## Message Protocol

All WebSocket messages use JSON envelope:

```json
{
  "type": "MESSAGE_TYPE",
  "data": { ... },
  "timestamp": 1234567890
}
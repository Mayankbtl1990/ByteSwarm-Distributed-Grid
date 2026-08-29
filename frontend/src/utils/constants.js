

export const WS_URL = 'ws://localhost:8080/swarm';

export const MESSAGE_TYPES = {
  REGISTERED: 'REGISTERED',
  COMPUTE_CHUNK: 'COMPUTE_CHUNK',
  CHUNK_RESULT: 'CHUNK_RESULT',
  HEARTBEAT: 'HEARTBEAT',
  ERROR: 'ERROR'
};

// Export MSG_TYPES alias to resolve hook import error
export const MSG_TYPES = MESSAGE_TYPES;

export const CONNECTION_STATUS = {
  DISCONNECTED: 'disconnected',
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  ERROR: 'error'
};
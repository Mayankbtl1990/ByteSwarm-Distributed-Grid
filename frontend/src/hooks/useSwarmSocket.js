import { useEffect, useState, useRef, useCallback } from 'react';
import { WS_URL, CONNECTION_STATUS, MESSAGE_TYPES } from '../utils/constants';

export function useSwarmSocket(onMessage) {
  const [status, setStatus] = useState(CONNECTION_STATUS.DISCONNECTED);
  const [workerId, setWorkerId] = useState(null);
  const socketRef = useRef(null);
  const reconnectRef = useRef(null);
  
  // Latest onMessage callback ko ref me store karein
  const onMessageRef = useRef(onMessage);
  useEffect(() => {
    onMessageRef.current = onMessage;
  }, [onMessage]);

  const connect = useCallback(() => {
    setStatus(CONNECTION_STATUS.CONNECTING);
    const ws = new WebSocket(WS_URL);
    socketRef.current = ws;

    ws.onopen = () => {
      console.log('[Swarm] Connected');
      setStatus(CONNECTION_STATUS.CONNECTED);
      ws.send(JSON.stringify({
        type: 'REGISTER',
        capabilities: {
          cores: navigator.hardwareConcurrency || 4,
          userAgent: navigator.userAgent
        }
      }));
    };

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        console.log('[Swarm]', msg);
        if (msg.type === MESSAGE_TYPES.REGISTERED) {
          setWorkerId(msg.workerId);
        }
        if (onMessageRef.current) onMessageRef.current(msg);
      } catch (err) {
        console.error('[Swarm] Parse error:', err);
      }
    };

    ws.onclose = () => {
      console.log('[Swarm] Disconnected. Retrying in 3s...');
      setStatus(CONNECTION_STATUS.DISCONNECTED);
      setWorkerId(null);
      reconnectRef.current = setTimeout(connect, 3000);
    };

    ws.onerror = () => setStatus(CONNECTION_STATUS.ERROR);
  }, []); // Ab dependency me onMessage ki zarurat nahi hai

  useEffect(() => {
    connect();
    return () => {
      if (reconnectRef.current) clearTimeout(reconnectRef.current);
      if (socketRef.current) socketRef.current.close();
    };
  }, [connect]);

  const send = useCallback((data) => {
    if (socketRef.current?.readyState === WebSocket.OPEN) {
      socketRef.current.send(JSON.stringify(data));
    }
  }, []);

  return { status, workerId, send };
}
import { useEffect, useState, useRef, useCallback } from 'react';
import { WS_URL, CONNECTION_STATUS, MESSAGE_TYPES } from '../utils/constants';

export function useSwarmSocket(onMessage) {
  const [status, setStatus] = useState(CONNECTION_STATUS.DISCONNECTED);
  const [workerId, setWorkerId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [stats, setStats] = useState({
    messagesReceived: 0,
    messagesSent: 0,
    connectedAt: null
  });
  const socketRef = useRef(null);
  const reconnectRef = useRef(null);

  const connect = useCallback(() => {
    setStatus(CONNECTION_STATUS.CONNECTING);
    const ws = new WebSocket(WS_URL);
    socketRef.current = ws;

    ws.onopen = () => {
      console.log('[Swarm] Connected');
      setStatus(CONNECTION_STATUS.CONNECTED);
      setStats(s => ({ ...s, connectedAt: Date.now() }));

      const registerMsg = {
        type: 'REGISTER',
        data: {
          cores: navigator.hardwareConcurrency || 4,
          userAgent: navigator.userAgent,
          platform: navigator.platform
        }
      };
      ws.send(JSON.stringify(registerMsg));
      setStats(s => ({ ...s, messagesSent: s.messagesSent + 1 }));
    };

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        console.log('[Swarm] ', msg);

        setMessages(prev => [...prev.slice(-49), { ...msg, receivedAt: Date.now() }]);
        setStats(s => ({ ...s, messagesReceived: s.messagesReceived + 1 }));

        if (msg.type === MESSAGE_TYPES.REGISTERED) {
          const wid = msg.data?.workerId || msg.workerId;
          setWorkerId(wid);
        }

        if (onMessage) onMessage(msg);
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
  }, [onMessage]);

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
      setStats(s => ({ ...s, messagesSent: s.messagesSent + 1 }));
    }
  }, []);

  return { status, workerId, send, messages, stats };
}
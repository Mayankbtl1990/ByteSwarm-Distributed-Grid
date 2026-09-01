import { useEffect, useRef, useState, useCallback } from 'react';
import { WS_URL } from '../utils/constants';

const MAX_RETRIES = 10;
const BASE_DELAY = 1000;
const HEARTBEAT_INTERVAL = 10000;

export function useSwarmSocket(onMessage) {
  const [status, setStatus] = useState('disconnected');
  const [messages, setMessages] = useState([]);
  const [reconnectAttempt, setReconnectAttempt] = useState(0);
  const [workerId, setWorkerId] = useState(null);
  const [stats, setStats] = useState({
    messagesReceived: 0,
    messagesSent: 0,
    connectedAt: null,
  });

  const wsRef = useRef(null);
  const retryRef = useRef(0);
  const shouldReconnect = useRef(true);
  const heartbeatRef = useRef(null);

  const stopHeartbeat = useCallback(() => {
    if (heartbeatRef.current) {
      clearInterval(heartbeatRef.current);
      heartbeatRef.current = null;
    }
  }, []);

  const send = useCallback((obj) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(obj));
      setStats((prev) => ({
        ...prev,
        messagesSent: prev.messagesSent + 1,
      }));
      return true;
    }
    return false;
  }, []);

  const startHeartbeat = useCallback(() => {
    stopHeartbeat();
    heartbeatRef.current = setInterval(() => {
      if (wsRef.current?.readyState === WebSocket.OPEN) {
        send({
          type: 'HEARTBEAT',
          data: { ts: Date.now() },
          timestamp: Date.now(),
        });
      }
    }, HEARTBEAT_INTERVAL);
  }, [send, stopHeartbeat]);

  const connect = useCallback(() => {
    setStatus(retryRef.current > 0 ? 'reconnecting' : 'connecting');

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log('[ws] connected');
      setStatus('connected');
      retryRef.current = 0;
      setReconnectAttempt(0);
      setStats((prev) => ({
        ...prev,
        connectedAt: Date.now(),
      }));

      ws.send(JSON.stringify({
        type: 'REGISTER',
        data: {
          userAgent: navigator.userAgent,
          cpuCores: navigator.hardwareConcurrency || 1,
          platform: navigator.platform,
        },
        timestamp: Date.now(),
      }));

      setStats((prev) => ({
        ...prev,
        messagesSent: prev.messagesSent + 1,
      }));

      startHeartbeat();
    };

    ws.onmessage = (evt) => {
      try {
        const msg = JSON.parse(evt.data);

        setMessages((m) => [...m.slice(-199), msg]);
        setStats((prev) => ({
          ...prev,
          messagesReceived: prev.messagesReceived + 1,
        }));

        if (msg.type === 'REGISTERED' && msg.data?.workerId) {
          setWorkerId(msg.data.workerId);
        }

        if (onMessage) {
          onMessage(msg);
        }
      } catch (e) {
        console.error('[ws] parse error', e);
      }
    };

    ws.onerror = (e) => {
      console.error('[ws] error', e);
      setStatus('error');
    };

    ws.onclose = (evt) => {
      console.warn('[ws] closed', evt.code, evt.reason);
      setStatus('disconnected');
      stopHeartbeat();

      if (!shouldReconnect.current) return;

      if (retryRef.current >= MAX_RETRIES) {
        console.error('[ws] giving up after', MAX_RETRIES, 'retries');
        return;
      }

      const delay = Math.min(BASE_DELAY * 2 ** retryRef.current, 15000);
      retryRef.current += 1;
      setReconnectAttempt(retryRef.current);

      console.log(`[ws] reconnecting in ${delay}ms (attempt ${retryRef.current})`);
      setTimeout(() => {
        if (shouldReconnect.current) {
          connect();
        }
      }, delay);
    };
  }, [onMessage, startHeartbeat, stopHeartbeat]);

  useEffect(() => {
    shouldReconnect.current = true;
    connect();

    return () => {
      shouldReconnect.current = false;
      stopHeartbeat();

      if (wsRef.current) {
        try {
          wsRef.current.close();
        } catch (e) {
          console.warn('[ws] close cleanup error', e);
        }
      }
    };
  }, [connect, stopHeartbeat]);

  return { status, messages, send, reconnectAttempt, workerId, stats };
}
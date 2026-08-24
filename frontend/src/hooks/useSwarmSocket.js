import { useEffect, useRef, useState, useCallback } from 'react';
import { WS_URL, MESSAGE_TYPES } from '../utils/constants';

const MAX_RETRIES = 10;
const BASE_DELAY = 1000;

export function useSwarmSocket() {
  const [status, setStatus] = useState('disconnected');
  const [messages, setMessages] = useState([]);
  const [reconnectAttempt, setReconnectAttempt] = useState(0);

  const wsRef = useRef(null);
  const retryRef = useRef(0);
  const shouldReconnect = useRef(true);

  const connect = useCallback(() => {
    setStatus(retryRef.current > 0 ? 'reconnecting' : 'connecting');

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log('[ws] connected');
      setStatus('connected');
      retryRef.current = 0;
      setReconnectAttempt(0);

      const workerId = `worker-${Math.random().toString(36).slice(2, 8)}`;
      ws.send(JSON.stringify({
        type: MESSAGE_TYPES.REGISTERED,
        data: { workerId, ua: navigator.userAgent },
        timestamp: Date.now(),
      }));
    };

    ws.onmessage = (evt) => {
      try {
        const msg = JSON.parse(evt.data);
        setMessages((m) => [...m.slice(-199), msg]);
      } catch (e) {
        console.error('[ws] parse error', e);
      }
    };

    ws.onerror = (e) => console.error('[ws] error', e);

    ws.onclose = (evt) => {
      console.warn('[ws] closed', evt.code, evt.reason);
      setStatus('disconnected');

      if (!shouldReconnect.current) return;

      if (retryRef.current >= MAX_RETRIES) {
        console.error('[ws] giving up after', MAX_RETRIES, 'retries');
        return;
      }

      const delay = Math.min(BASE_DELAY * 2 ** retryRef.current, 15000);
      retryRef.current += 1;
      setReconnectAttempt(retryRef.current);

      console.log(`[ws] reconnecting in ${delay}ms (attempt ${retryRef.current})`);
      setTimeout(connect, delay);
    };
  }, []);

  useEffect(() => {
    shouldReconnect.current = true;
    connect();

    return () => {
      shouldReconnect.current = false;
      if (wsRef.current) wsRef.current.close();
    };
  }, [connect]);

  const send = useCallback((obj) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(obj));
    }
  }, []);

  return { status, messages, send, reconnectAttempt };
}
import React, { useCallback, useRef, useState, useEffect } from 'react';
import { useSwarmSocket } from '../hooks/useSwarmSocket';

export default function WorkerNode({ onChunkProcessed }) {
  const workerRef = useRef(null);
  const [computed, setComputed] = useState(0);
  const [currentChunk, setCurrentChunk] = useState(null);
  const sendRef = useRef(null);

  useEffect(() => {
    workerRef.current = new Worker(
      new URL('../workers/computeWorker.js', import.meta.url),
      { type: 'module' }
    );

    workerRef.current.onmessage = (e) => {
      const { chunkId, jobId, results, computeTimeMs } = e.data;
      console.log(`[WorkerNode]  Chunk ${chunkId} done in ${computeTimeMs.toFixed(1)}ms`);
      if (sendRef.current) {
        sendRef.current({
          type: 'CHUNK_RESULT',
          data: {
            chunkId,
            jobId,
            results,
            computeTimeMs
          }
        });
      }

      setComputed(c => c + 1);
      setCurrentChunk(null);
      if (onChunkProcessed) onChunkProcessed(chunkId);
    };

    return () => {
      if (workerRef.current) workerRef.current.terminate();
    };
  }, [onChunkProcessed]);

  const handleMessage = useCallback((msg) => {
    if (msg.type === 'COMPUTE_CHUNK') {
      const chunk = msg.data;
      console.log('[WorkerNode]  Received chunk:', chunk.chunkId);
      setCurrentChunk(chunk);
      workerRef.current.postMessage(chunk);
    }
  }, []);

  const { status, workerId, send } = useSwarmSocket(handleMessage);
  sendRef.current = send;

  const statusColor = {
    connected: '#0f0',
    connecting: '#ff0',
    disconnected: '#f80',
    error: '#f00'
  }[status] || '#888';

  return (
    <div className="worker-card">
      <div className="worker-header">
        <span className="worker-icon"> </span>
        <span>Worker Node</span>
        <span className="worker-status-dot"
              style={{ background: statusColor, boxShadow: `0 0 10px ${statusColor}` }}></span>
      </div>
      <div className="worker-body">
        <Row label="Status" value={status.toUpperCase()} color={statusColor} />
        <Row label="Worker ID" value={workerId || '—'} />
        <Row label="CPU Cores" value={navigator.hardwareConcurrency || 'N/A'} />
        <Row label="Chunks computed" value={computed} color="#0f0" />
        <Row label="Currently processing"
             value={currentChunk?.chunkId?.slice(-8) || 'idle'}
             color={currentChunk ? '#fc0' : '#666'} />
      </div>
    </div>
  );
}

function Row({ label, value, color }) {
  return (
    <div className="worker-row">
      <span className="worker-label">{label}</span>
      <span className="worker-value mono" style={color ? { color } : {}}>{value}</span>
    </div>
  );
}
import React, { useCallback } from 'react';
import { useSwarmSocket } from '../hooks/useSwarmSocket';

export default function WorkerNode() {
  const handleMessage = useCallback((msg) => {
    if (msg.type === 'COMPUTE_CHUNK') {
      console.log('[Worker] Received chunk (compute coming Week 2):', msg);
    }
  }, []);

  const { status, workerId } = useSwarmSocket(handleMessage);

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
        <span className="worker-status-dot" style={{ background: statusColor, boxShadow: `0 0 10px ${statusColor}` }}></span>
      </div>
      <div className="worker-body">
        <Row label="Status" value={status.toUpperCase()} color={statusColor} />
        <Row label="Worker ID" value={workerId || '—'} />
        <Row label="CPU Cores" value={navigator.hardwareConcurrency || 'N/A'} />
        <Row label="Platform" value={navigator.platform} />
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
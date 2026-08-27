import React from 'react';
import { useSwarmMetrics } from '../hooks/useSwarmMetrics';

function getWorkerState(node) {
  if (node.state) return node.state;
  if (node.heartbeatAgeMs > 15000) return 'STALE';
  return node.busy ? 'BUSY' : 'IDLE';
}

function getWorkerColor(state) {
  switch (state) {
    case 'BUSY':
      return '#fc0';
    case 'STALE':
      return '#f44';
    case 'IDLE':
    default:
      return '#0f0';
  }
}

export default function WorkerGrid() {
  const { nodes } = useSwarmMetrics();
  const safeNodes = Array.isArray(nodes) ? nodes : [];

  if (safeNodes.length === 0) {
    return (
      <div className="worker-grid-empty">
        <div className="empty-icon">🖥️</div>
        <div>No workers connected to the swarm yet</div>
      </div>
    );
  }

  return (
    <div>
      <div className="worker-grid-header">
        <span>Connected Workers</span>
        <span className="mono" style={{ color: '#666' }}>
          {safeNodes.length} nodes
        </span>
      </div>

      <div className="worker-grid">
        {safeNodes.map((n) => {
          const state = getWorkerState(n);
          const color = getWorkerColor(state);

          return (
            <div key={n.id} className={`worker-tile ${state === 'BUSY' ? 'tile-busy' : 'tile-idle'}`}>
              <div className="tile-header">
                <span className="tile-icon">
                  {state === 'BUSY' ? '⚡' : state === 'STALE' ? '🔴' : '🟢'}
                </span>
                <span className="tile-id mono">{(n.id || '').slice(0, 8)}</span>
              </div>

              <div className="tile-body">
                <div className="tile-stat">
                  <span className="tile-label">Chunks</span>
                  <span className="tile-value mono">{n.chunksProcessed || 0}</span>
                </div>

                <div className="tile-stat">
                  <span className="tile-label">State</span>
                  <span className="tile-value mono" style={{ color }}>
                    {state}
                  </span>
                </div>

                <div className="tile-stat">
                  <span className="tile-label">Heartbeat</span>
                  <span className="tile-value mono">
                    {typeof n.heartbeatAgeMs === 'number'
                      ? `${Math.floor(n.heartbeatAgeMs / 1000)}s ago`
                      : '—'}
                  </span>
                </div>

                <div
                  className="tile-status"
                  style={{
                    background: color,
                    boxShadow: `0 0 8px ${color}`
                  }}
                ></div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
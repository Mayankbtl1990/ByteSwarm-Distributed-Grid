import React from 'react';
import { useSwarmMetrics } from '../hooks/useSwarmMetrics';

export default function WorkerGrid() {
  const { nodes } = useSwarmMetrics();

  if (!nodes || nodes.length === 0) {
    return (
      <div className="worker-grid-empty">
        <div className="empty-icon"> </div>
        <div>No workers connected to the swarm yet</div>
      </div>
    );
  }

  return (
    <div>
      <div className="worker-grid-header">
        <span> Connected Workers</span>
        <span className="mono" style={{ color: '#666' }}>{nodes.length} nodes</span>
      </div>
      <div className="worker-grid">
        {nodes.map((n) => (
          <div key={n.id} className={`worker-tile ${n.busy ? 'tile-busy' : 'tile-idle'}`}>
            <div className="tile-header">
              <span className="tile-icon">{n.busy ? ' ' : ' '}</span>
              <span className="tile-id mono">{n.id.slice(0, 8)}</span>
            </div>
            <div className="tile-body">
              <div className="tile-stat">
                <span className="tile-label">Chunks</span>
                <span className="tile-value mono">{n.chunksProcessed || 0}</span>
              </div>
              <div className="tile-status" style={{
                background: n.busy ? '#fc0' : '#0f0',
                boxShadow: n.busy ? '0 0 8px #fc0' : '0 0 8px #0f0'
              }}></div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
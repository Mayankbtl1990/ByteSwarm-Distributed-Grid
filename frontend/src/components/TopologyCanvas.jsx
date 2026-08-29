import React from 'react';
import { useSwarmMetrics } from '../hooks/useSwarmMetrics';

function getState(node) {
  if (node.state) return node.state;
  if (node.heartbeatAgeMs > 15000) return 'STALE';
  return node.busy ? 'BUSY' : 'IDLE';
}

function getColor(state) {
  switch (state) {
    case 'BUSY':
      return '#fc0';
    case 'STALE':
      return '#f44';
    default:
      return '#0f0';
  }
}

export default function TopologyCanvas() {
  const { nodes } = useSwarmMetrics();
  const safeNodes = Array.isArray(nodes) ? nodes : [];

  return (
    <section className="topology-panel">
      <h2 style={{ color: '#0f0', marginBottom: 15 }}>Swarm Topology</h2>

      <div className="topology-master">MASTER NODE</div>

      <div className="topology-workers">
        {safeNodes.length === 0 ? (
          <div className="topology-empty">No worker nodes connected</div>
        ) : (
          safeNodes.map((node) => {
            const state = getState(node);
            const color = getColor(state);

            return (
              <div key={node.id} className="topology-node-wrapper">
                <div className="topology-link" />
                <div
                  className="topology-node"
                  style={{
                    borderColor: color,
                    boxShadow: `0 0 12px ${color}`,
                  }}
                >
                  <div className="topology-node-id mono">{(node.id || '').slice(0, 8)}</div>
                  <div className="topology-node-state" style={{ color }}>
                    {state}
                  </div>
                  <div className="topology-node-meta mono">
                    {node.chunksProcessed || 0} chunks
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </section>
  );
}
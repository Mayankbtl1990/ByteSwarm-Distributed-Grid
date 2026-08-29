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
  const { nodes, metrics } = useSwarmMetrics();
  const safeNodes = Array.isArray(nodes) ? nodes : [];
  const safeMetrics = metrics || {};

  return (
    <section className="topology-panel">
      <h2 style={{ color: '#0f0', marginBottom: 15 }}>Swarm Topology</h2>

      <div className="topology-master">
        <div style={{ fontWeight: 700 }}>MASTER NODE</div>
        <div className="mono" style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
          active={safeMetrics.activeWorkers ?? safeNodes.length} | busy={safeMetrics.busyWorkers ?? 0} | stale={safeMetrics.staleWorkers ?? 0}
        </div>
      </div>

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
                    background: 'rgba(0,0,0,0.35)'
                  }}
                >
                  <div className="topology-node-id mono">{(node.id || '').slice(0, 8)}</div>
                  <div className="topology-node-state" style={{ color }}>
                    {state}
                  </div>
                  <div className="topology-node-meta mono">
                    {node.chunksProcessed || 0} chunks
                  </div>
                  <div className="topology-node-meta mono">
                    {typeof node.heartbeatAgeMs === 'number'
                      ? `${Math.floor(node.heartbeatAgeMs / 1000)}s heartbeat`
                      : '—'}
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
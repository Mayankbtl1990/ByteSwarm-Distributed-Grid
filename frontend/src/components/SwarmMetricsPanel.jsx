 import React from 'react';
import { useSwarmMetrics } from '../hooks/useSwarmMetrics';

export default function SwarmMetricsPanel() {
  const { nodes, metrics, jobs, error } = useSwarmMetrics();

  const runningJobs = jobs.filter(j => j.state === 'RUNNING').length;
  const completedJobs = jobs.filter(j => j.state === 'COMPLETED').length;

  const totalCompleted = jobs.reduce((sum, j) => sum + (j.completedChunks || 0), 0);
  const totalChunks = jobs.reduce((sum, j) => sum + (j.totalChunks || 0), 0);

  return (
    <div className="swarm-panel">
      <div className="swarm-header">
        <span> Global Swarm Metrics</span>
        {error && <span className="swarm-error"> {error}</span>}
      </div>
      <div className="swarm-grid">
        <SwarmStat icon="" label="Active Workers" value={metrics.activeWorkers} color="#0f0" />
        <SwarmStat icon="" label="Total Jobs" value={metrics.totalJobs} color="#fc0" />
        <SwarmStat icon="" label="Running" value={runningJobs} color="#f80" />
        <SwarmStat icon="" label="Completed" value={completedJobs} color="#0af" />
        <SwarmStat icon="" label="Chunks Done" value={totalCompleted} color="#0f0" />
        <SwarmStat icon="" label="Total Chunks" value={totalChunks} color="#888" />
      </div>
    </div>
  );
}

function SwarmStat({ icon, label, value, color }) {
  return (
    <div className="swarm-stat">
      <div className="swarm-stat-icon">{icon}</div>
      <div className="swarm-stat-label">{label}</div>
      <div className="swarm-stat-value mono" style={{ color: color || '#0f0' }}>{value}</div>
    </div>
  );
}
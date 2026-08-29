import React from 'react';
import { useSwarmMetrics } from '../hooks/useSwarmMetrics';

export default function SwarmMetricsPanel() {
  const { nodes, metrics, jobs, error } = useSwarmMetrics();

  const safeJobs = Array.isArray(jobs) ? jobs : [];
  const safeMetrics = metrics || {};
  const safeNodes = Array.isArray(nodes) ? nodes : [];

  const runningJobs = safeJobs.filter(j => j.state === 'RUNNING').length;
  const completedJobs = safeJobs.filter(j => j.state === 'COMPLETED').length;

  const totalCompleted = safeJobs.reduce((sum, j) => sum + (j.completedChunks || 0), 0);
  const totalChunks = safeJobs.reduce((sum, j) => sum + (j.totalChunks || 0), 0);

  return (
    <div className="swarm-panel">
      <div className="swarm-header">
        <span>Global Swarm Metrics</span>
        {error && <span className="swarm-error">{error}</span>}
      </div>

      <div className="swarm-grid">
        <SwarmStat label="Active Workers" value={safeMetrics.activeWorkers ?? safeNodes.length} color="#0f0" />
        <SwarmStat label="Busy Workers" value={safeMetrics.busyWorkers ?? 0} color="#fc0" />
        <SwarmStat label="Idle Workers" value={safeMetrics.idleWorkers ?? 0} color="#0af" />
        <SwarmStat label="Stale Workers" value={safeMetrics.staleWorkers ?? 0} color="#f44" />
        <SwarmStat label="Total Jobs" value={safeMetrics.totalJobs ?? safeJobs.length} color="#fc0" />
        <SwarmStat label="Running" value={runningJobs} color="#f80" />
        <SwarmStat label="Completed" value={completedJobs} color="#0af" />
        <SwarmStat label="Chunks Done" value={safeMetrics.completedChunks ?? totalCompleted} color="#0f0" />
        <SwarmStat label="Total Chunks" value={safeMetrics.totalChunks ?? totalChunks} color="#888" />
        <SwarmStat label="Reassigned" value={safeMetrics.reassignedChunks ?? 0} color="#ff66cc" />
        <SwarmStat label="Dropped Workers" value={safeMetrics.droppedWorkers ?? 0} color="#f44" />
      </div>
    </div>
  );
}

function SwarmStat({ label, value, color }) {
  return (
    <div className="swarm-stat">
      <div className="swarm-stat-label">{label}</div>
      <div className="swarm-stat-value mono" style={{ color: color || '#0f0' }}>
        {value}
      </div>
    </div>
  );
}
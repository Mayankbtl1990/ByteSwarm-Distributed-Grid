import React from 'react';

export default function StatsPanel({ stats, status }) {
  const uptime = stats.connectedAt
    ? Math.floor((Date.now() - stats.connectedAt) / 1000)
    : 0;

  return (
    <div className="stats-grid">
      <StatCard icon="" label="Messages Received" value={stats.messagesReceived} />
      <StatCard icon="" label="Messages Sent" value={stats.messagesSent} />
      <StatCard icon="" label="Uptime (sec)" value={uptime} />
      <StatCard icon="" label="Status" value={status.toUpperCase()}
                color={status === 'connected' ? '#0f0' : '#f80'} />
    </div>
  );
}

function StatCard({ icon, label, value, color }) {
  return (
    <div className="stat-card">
      <div className="stat-icon">{icon}</div>
      <div className="stat-label">{label}</div>
      <div className="stat-value mono" style={color ? { color } : {}}>{value}</div>
    </div>
  );
}
import React from 'react';

export default function ProgressBar({ current, total, label }) {
  const percent = total > 0 ? Math.min(100, (current / total) * 100) : 0;

  return (
    <div className="progress-container">
      <div className="progress-header">
        <span>{label}</span>
        <span className="mono">{current} / {total} ({percent.toFixed(1)}%)</span>
      </div>
      <div className="progress-track">
        <div className="progress-fill" style={{ width: `${percent}%` }}>
          <div className="progress-shine"></div>
        </div>
      </div>
    </div>
  );
}
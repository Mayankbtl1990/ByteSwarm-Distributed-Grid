import React from 'react';
import './TopologyCanvas.css';

export default function TopologyCanvas({ workers = [] }) {
  return (
    <section className="topology">
      <div className="topology__header">
        <span className="topology__title"> SWARM TOPOLOGY</span>
        <span className="topology__count">{workers.length} nodes</span>
      </div>
      <div className="topology__stage">
        <svg viewBox="0 0 600 400" className="topology__svg">
          <circle cx="300" cy="200" r="30" className="topology__core" />
          <text x="300" y="205" textAnchor="middle" className="topology__core-label">
            HUB
          </text>
        </svg>
        {workers.length === 0 && (
          <div className="topology__empty">Waiting for workers to connect…</div>
        )}
      </div>
    </section>
  );
}
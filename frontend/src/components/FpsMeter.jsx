import React from 'react';
import { useFpsMonitor } from '../hooks/useFpsMonitor';

export default function FpsMeter() {
  const fps = useFpsMonitor();
  const color = fps >= 55 ? '#0f0' : fps >= 30 ? '#fc0' : '#f00';
  const label = fps >= 55 ? 'SMOOTH' : fps >= 30 ? 'OK' : 'LAGGY';

  return (
    <div className="fps-meter" style={{ borderColor: color }}>
      <div className="fps-value mono" style={{ color }}>{fps}</div>
      <div className="fps-label">FPS · {label}</div>
    </div>
  );
}
import React from 'react';

export default function StatusBar() {
  return (
    <div className="status-bar" style={{ padding: '10px', background: '#111', color: '#0f0', borderBottom: '1px solid #333' }}>
      <span>System Status: Online</span>
    </div>
  );
}
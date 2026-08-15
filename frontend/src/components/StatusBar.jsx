import React, { useState, useEffect } from 'react';

export default function StatusBar() {
  const [time, setTime] = useState(new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date().toLocaleTimeString()), 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="status-bar">
      <div className="status-item">
        <span className="dot dot-green"></span>
        <span>System Online</span>
      </div>
      <div className="status-item">
        <span className="mono">🕐 {time}</span>
      </div>
      <div className="status-item">
        <span className="mono">v1.0.0 — Week 1</span>
      </div>
    </div>
  );
}
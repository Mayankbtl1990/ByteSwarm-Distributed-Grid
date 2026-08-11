import React from 'react';
import './App.css';

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <div className="logo"></div>
        <div>
          <h1 className="app-title">BYTESWARM</h1>
          <p className="app-subtitle">Browser-Based Distributed Compute Grid</p>
        </div>
      </header>

      <main className="app-main">
        <h2 style={{ color: '#0f0' }}>Day 1 — Base UI Ready</h2>
      </main>

      <footer className="app-footer">
        Team ByteSwarm • Bhanu • Balaji • Mayank • Mansi
      </footer>
    </div>
  );
}
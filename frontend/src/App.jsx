import React from 'react';
import StatusBar from './components/StatusBar';
import './App.css';

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <div className="logo"> </div>
        <div>
          <h1 className="app-title">BYTESWARM</h1>
          <p className="app-subtitle">Browser-Based Distributed Compute Grid</p>
        </div>
      </header>

      <StatusBar />

      <main className="app-main">
        <h2 style={{ color: '#0f0' }}>Day 2 — Status Bar Added</h2>
        <p style={{ color: '#888', marginTop: 10 }}>WorkerNode component arriving Day 3.</p>
      </main>

      <footer className="app-footer">Team ByteSwarm • Bhanu • Balaji • Mayank • Mansi</footer>
    </div>
  );
}
import React from 'react';
import StatusBar from './components/StatusBar';
import WorkerNode from './components/WorkerNode';
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
        <section>
          <h2 style={{ color: '#0f0', marginBottom: 15 }}>Your Worker Node</h2>
          <p style={{ color: '#888', marginBottom: 20 }}>
            This browser tab is contributing spare CPU power to the swarm.
          </p>
          <WorkerNode />
        </section>
      </main>

      <footer className="app-footer">Team ByteSwarm • Bhanu • Balaji • Mayank • Mansi</footer>
    </div>
  );
}
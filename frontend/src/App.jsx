import React, { useState, useCallback } from 'react';
import StatusBar from './components/StatusBar';
import WorkerNode from './components/WorkerNode';
import MessageLog from './components/MessageLog';
import { useSwarmSocket } from './hooks/useSwarmSocket';
import './App.css';

export default function App() {
  const [chunks, setChunks] = useState([]);
  const handleMessage = useCallback((msg) => {
    if (msg.type === 'COMPUTE_CHUNK') setChunks(c => [...c, msg]);
  }, []);
  const { messages } = useSwarmSocket(handleMessage);

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
          <WorkerNode />
          <MessageLog messages={messages} />
        </section>
      </main>

      <footer className="app-footer">Team ByteSwarm • Bhanu • Balaji • Mayank • Mansi</footer>
    </div>
  );
}
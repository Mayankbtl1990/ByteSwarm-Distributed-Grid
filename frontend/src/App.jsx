import React, { useState, useCallback } from 'react';
import StatusBar from './components/StatusBar';
import WorkerNode from './components/WorkerNode';
import MessageLog from './components/MessageLog';
import StatsPanel from './components/StatsPanel';
import JobsSection from './components/JobsSection';
import SwarmMetricsPanel from './components/SwarmMetricsPanel';
import WorkerGrid from './components/WorkerGrid';
import { useSwarmSocket } from './hooks/useSwarmSocket';
import './App.css';

export default function App() {
  const [activeJob, setActiveJob] = useState(null);
  const [chunkStats, setChunkStats] = useState({ completed: 0, total: 0 });

  const handleMessage = useCallback((msg) => {
    if (msg.type === 'COMPUTE_CHUNK') {
      setActiveJob({ jobId: msg.data?.jobId, jobType: 'compute' });
      setChunkStats(s => ({ ...s, total: s.total + 1 }));
    }
  }, []);

  const handleChunkProcessed = useCallback(() => {
    setChunkStats(s => ({ ...s, completed: s.completed + 1 }));
  }, []);

  const { messages, stats, status } = useSwarmSocket(handleMessage);

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
        <SwarmMetricsPanel />
        <WorkerGrid />
        <StatsPanel stats={stats} status={status} />
        <section>
          <h2 style={{ color: '#0f0', marginBottom: 15 }}>Your Worker Node</h2>
          <WorkerNode onChunkProcessed={handleChunkProcessed} />
          <JobsSection activeJob={activeJob} chunkStats={chunkStats} />
          <MessageLog messages={messages} />
        </section>
      </main>

      <footer className="app-footer">Team ByteSwarm • Bhanu • Balaji • Mayank • Mansi</footer>
    </div>
  );
}
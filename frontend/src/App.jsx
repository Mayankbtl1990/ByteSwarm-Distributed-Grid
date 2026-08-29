import React, { useState, useCallback } from 'react';
import StatusBar from './components/StatusBar';
import WorkerNode from './components/WorkerNode';
import MessageLog from './components/MessageLog';
import JobsSection from './components/JobsSection';
import SwarmMetricsPanel from './components/SwarmMetricsPanel';
import WorkerGrid from './components/WorkerGrid';
import FpsMeter from './components/FpsMeter';
import './App.css';
import TopologyCanvas from './components/TopologyCanvas';

export default function App() {
  const [activeJob, setActiveJob] = useState(null);
  const [chunkStats, setChunkStats] = useState({ completed: 0, total: 0 });
  const [localMessages, setLocalMessages] = useState([]);

  const handleChunkAssigned = useCallback((chunk) => {
    setActiveJob({ jobId: chunk?.jobId, jobType: 'compute' });
    setChunkStats((s) => ({ ...s, total: s.total + 1 }));
    setLocalMessages((m) => [
      ...m.slice(-199),
      {
        type: 'COMPUTE_CHUNK',
        data: chunk,
        timestamp: Date.now(),
      },
    ]);
  }, []);

  const handleChunkProcessed = useCallback((result) => {
    setChunkStats((s) => ({ ...s, completed: s.completed + 1 }));
    setLocalMessages((m) => [
      ...m.slice(-199),
      {
        type: 'CHUNK_RESULT',
        data: result,
        timestamp: Date.now(),
      },
    ]);
  }, []);

  return (
    <div className="app">
      <FpsMeter />

      <header className="app-header">
        <div className="logo">⚙️</div>
        <div>
          <h1 className="app-title">BYTESWARM</h1>
          <p className="app-subtitle">Browser-Based Distributed Compute Grid</p>
        </div>
      </header>

      <StatusBar />

      <main className="app-main">
        <SwarmMetricsPanel />
        <WorkerGrid />

        <section>
          <h2 style={{ color: '#0f0', marginBottom: 15 }}>Your Worker Node</h2>
          <WorkerNode
            onChunkProcessed={handleChunkProcessed}
            onChunkAssigned={handleChunkAssigned}
          />
          <JobsSection activeJob={activeJob} chunkStats={chunkStats} />
          <TopologyCanvas />
          <MessageLog messages={localMessages} />
        </section>
      </main>

      <footer className="app-footer">Team ByteSwarm • Bhanu • Mayank • Mansi • Balaji</footer>
    </div>
  );
}
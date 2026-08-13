import React from 'react';

export default function ChunkExecution({ currentChunk, processedCount }) {
  return (
    <div className="chunk-card" style={{ background: '#1a1a1a', padding: '15px', borderRadius: '8px', border: '1px solid #333', marginTop: '15px' }}>
      <h3 style={{ color: '#C83F12', marginTop: 0 }}>Active Compute Status</h3>
      <p style={{ color: '#fff' }}>Processed Chunks: <strong>{processedCount}</strong></p>
      {currentChunk ? (
        <div style={{ color: '#FFF287' }}>
          <p>Processing Chunk ID: <code>{currentChunk.id || 'N/A'}</code></p>
          <p>Data Size: {currentChunk.size || 0} items</p>
        </div>
      ) : (
        <p style={{ color: '#888' }}>Waiting for incoming chunk from swarm...</p>
      )}
    </div>
  );
}
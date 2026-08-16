self.onmessage = (e) => {
  const { chunkId, jobId, payload } = e.data;
  const startTime = performance.now();

  console.log(`[Worker] 🔧 Processing chunk ${chunkId} (${payload.length} items)`);

  const results = payload.map((equation, idx) => {
    try {
      const value = Function(`"use strict"; return (${equation});`)();
      return { input: equation, output: value, index: idx };
    } catch (err) {
      return { input: equation, error: err.message, index: idx };
    }
  });

  const elapsed = performance.now() - startTime;
  console.log(`[Worker]  Completed chunk ${chunkId} in ${elapsed.toFixed(2)}ms`);

  self.postMessage({
    chunkId,
    jobId,
    results,
    computeTimeMs: elapsed,
    completedAt: Date.now()
  });
};

self.onerror = (err) => {
  console.error('[Worker]  Error:', err);
};
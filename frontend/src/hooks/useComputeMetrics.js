import { useState, useCallback } from 'react';

export function useComputeMetrics() {
  const [metrics, setMetrics] = useState({
    chunksCompleted: 0,
    totalComputeMs: 0,
    avgComputeMs: 0,
    lastComputeMs: 0,
    estimatedGFlops: 0,
    lastChunkAt: null
  });

  const recordChunk = useCallback((computeMs, itemCount = 1000) => {
    setMetrics(prev => {
      const chunksCompleted = prev.chunksCompleted + 1;
      const totalComputeMs = prev.totalComputeMs + computeMs;
      const avgComputeMs = totalComputeMs / chunksCompleted;
      const opsPerSecond = (itemCount * 3) / (computeMs / 1000);
      const estimatedGFlops = opsPerSecond / 1e9;

      return {
        chunksCompleted,
        totalComputeMs,
        avgComputeMs,
        lastComputeMs: computeMs,
        estimatedGFlops,
        lastChunkAt: Date.now()
      };
    });
  }, []);

  const reset = useCallback(() => {
    setMetrics({
      chunksCompleted: 0,
      totalComputeMs: 0,
      avgComputeMs: 0,
      lastComputeMs: 0,
      estimatedGFlops: 0,
      lastChunkAt: null
    });
  }, []);

  return { metrics, recordChunk, reset };
}
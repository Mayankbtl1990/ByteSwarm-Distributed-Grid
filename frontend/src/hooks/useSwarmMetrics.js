import { useEffect, useState } from 'react';

export function useSwarmMetrics() {
  const [data, setData] = useState({
    nodes: [],
    metrics: { activeWorkers: 0, totalJobs: 0 },
    jobs: []
  });
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const fetchMetrics = async () => {
      try {
        const res = await fetch('http://localhost:8081/api/metrics');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        if (!cancelled) {
          setData(json);
          setError(null);
        }
      } catch (err) {
        if (!cancelled) setError(err.message);
      }
    };

    fetchMetrics();
    const interval = setInterval(fetchMetrics, 2000);

    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, []);

  return { ...data, error };
}
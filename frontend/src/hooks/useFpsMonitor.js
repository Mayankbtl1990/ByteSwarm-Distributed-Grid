import { useEffect, useState } from 'react';

export function useFpsMonitor() {
  const [fps, setFps] = useState(60);

  useEffect(() => {
    let frames = 0;
    let lastTime = performance.now();
    let rafId;

    const tick = (now) => {
      frames++;
      if (now - lastTime >= 1000) {
        setFps(Math.round((frames * 1000) / (now - lastTime)));
        frames = 0;
        lastTime = now;
      }
      rafId = requestAnimationFrame(tick);
    };

    rafId = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafId);
  }, []);

  return fps;
}
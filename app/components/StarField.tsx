"use client";
import { useMemo } from "react";

export default function StarField() {
  const stars = useMemo(() => {
    return Array.from({ length: 80 }, (_, i) => ({
      id: i,
      top: `${Math.random() * 100}%`,
      left: `${Math.random() * 100}%`,
      size: Math.random() * 2.5 + 0.8,
      dur: `${Math.random() * 3 + 2}s`,
      delay: `${Math.random() * 3}s`,
      opacity: Math.random() * 0.6 + 0.2,
    }));
  }, []);

  const particles = useMemo(() => {
    return Array.from({ length: 18 }, (_, i) => ({
      id: i,
      left: `${Math.random() * 100}%`,
      bottom: `${Math.random() * 30}%`,
      dur: `${Math.random() * 3 + 3}s`,
      delay: `${Math.random() * 4}s`,
    }));
  }, []);

  return (
    <div className="absolute inset-0 overflow-hidden pointer-events-none">
      {/* gradient sky */}
      <div
        className="absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 80% 60% at 50% 0%, #1B3A6A 0%, #0B1A2E 70%)",
        }}
      />
      {/* stars */}
      {stars.map((s) => (
        <div
          key={s.id}
          className="absolute rounded-full bg-white star"
          style={{
            top: s.top,
            left: s.left,
            width: s.size,
            height: s.size,
            opacity: s.opacity,
            ["--dur" as string]: s.dur,
            animationDelay: s.delay,
          }}
        />
      ))}
      {/* gold particles */}
      {particles.map((p) => (
        <div
          key={p.id}
          className="absolute rounded-full particle"
          style={{
            left: p.left,
            bottom: p.bottom,
            width: 3,
            height: 3,
            background: "var(--color-gold)",
            opacity: 0.7,
            ["--dur" as string]: p.dur,
            animationDelay: p.delay,
          }}
        />
      ))}
      {/* horizon glow */}
      <div
        className="absolute bottom-0 left-0 right-0 h-32"
        style={{
          background:
            "linear-gradient(to top, rgba(45,138,106,0.18) 0%, transparent 100%)",
        }}
      />
    </div>
  );
}

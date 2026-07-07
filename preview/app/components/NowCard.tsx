"use client";
import { useState } from "react";
import GeomOrnament from "./GeomOrnament";

const STATIONS = [
  { id: 1, label: "البسملة",    done: true },
  { id: 2, label: "الصلاة",     done: true },
  { id: 3, label: "الذكر",      done: false, active: true },
  { id: 4, label: "الدعاء",     done: false },
  { id: 5, label: "الاستغفار",  done: false },
];

export default function NowCard() {
  const [pressed, setPressed] = useState(false);
  const progress = STATIONS.filter((s) => s.done).length / STATIONS.length;

  return (
    <section
      className="px-4 section-enter"
      style={{ animationDelay: "0.38s" }}
      aria-label="رفيق اليوم"
    >
      {/* Section label */}
      <div className="flex items-center gap-2 mb-3">
        <div className="w-1 h-5 rounded-full" style={{ background: "var(--color-gold)" }} />
        <span className="text-[var(--color-gold)] text-sm font-semibold">رفيق اليوم</span>
        <div className="flex-1 h-px" style={{ background: "linear-gradient(to left, transparent, var(--color-gold-dim))" }} />
        <GeomOrnament size={14} opacity={0.4} />
      </div>

      <div
        className="relative rounded-2xl overflow-hidden glow-border-anim"
        style={{
          background: "linear-gradient(135deg, rgba(45,138,106,0.22) 0%, rgba(27,49,85,0.9) 50%, rgba(201,168,76,0.12) 100%)",
          border: "1px solid var(--color-border)",
        }}
      >
        {/* bg ornament */}
        <GeomOrnament size={140} className="absolute -bottom-8 -left-8" opacity={0.07} />
        <GeomOrnament size={80}  className="absolute top-2 right-2"     opacity={0.05} />

        <div className="relative z-10 p-4">
          {/* Header */}
          <div className="flex items-start justify-between mb-3">
            <div>
              <p className="text-[var(--color-muted)] text-xs">محطتك الحالية</p>
              <p className="text-[var(--color-foreground)] text-lg font-semibold mt-0.5">
                أذكار الصلاة
              </p>
            </div>
            <span
              className="text-xs px-2.5 py-1 rounded-full font-medium"
              style={{
                background: "rgba(201,168,76,0.15)",
                color: "var(--color-gold)",
                border: "1px solid rgba(201,168,76,0.3)",
              }}
            >
              الجزء الثاني
            </span>
          </div>

          {/* Station dots */}
          <div className="flex items-center gap-2 mb-4" role="list" aria-label="محطات اليوم">
            {STATIONS.map((s) => (
              <div key={s.id} className="flex flex-col items-center gap-1" role="listitem">
                <div
                  className="relative w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-bold"
                  style={{
                    background: s.done
                      ? "var(--color-emerald)"
                      : s.active
                      ? "var(--color-gold)"
                      : "rgba(255,255,255,0.07)",
                    border: s.active ? "2px solid var(--color-gold-light)" : "none",
                    color: s.done || s.active ? "#fff" : "var(--color-muted)",
                  }}
                >
                  {s.done ? "✓" : s.id}
                  {s.active && (
                    <span
                      className="pulse-ring absolute inset-0 rounded-full"
                      style={{ border: "2px solid var(--color-gold)", opacity: 0.5 }}
                      aria-hidden="true"
                    />
                  )}
                </div>
                <span className="text-[8px] text-[var(--color-muted)] whitespace-nowrap">{s.label}</span>
              </div>
            ))}
          </div>

          {/* Progress */}
          <div className="mb-4">
            <div className="flex justify-between text-[10px] text-[var(--color-muted)] mb-1">
              <span>التقدم</span>
              <span style={{ color: "var(--color-gold)" }}>{Math.round(progress * 100)}%</span>
            </div>
            <div className="h-2 rounded-full overflow-hidden" style={{ background: "rgba(255,255,255,0.07)" }}>
              <div
                className="h-full rounded-full shimmer-bar transition-all duration-700"
                style={{ width: `${progress * 100}%` }}
              />
            </div>
          </div>

          {/* CTA */}
          <button
            onPointerDown={() => setPressed(true)}
            onPointerUp={() => setPressed(false)}
            onPointerLeave={() => setPressed(false)}
            className="w-full py-3 rounded-xl font-semibold text-sm transition-transform duration-150"
            style={{
              background: "linear-gradient(135deg, var(--color-gold) 0%, var(--color-gold-light) 100%)",
              color: "#0B1A2E",
              transform: pressed ? "scale(0.97)" : "scale(1)",
              boxShadow: pressed ? "none" : "0 4px 20px rgba(201,168,76,0.4)",
            }}
            aria-label="ابدأ رفيق اليوم"
          >
            ابدأ الآن ←
          </button>
        </div>
      </div>
    </section>
  );
}

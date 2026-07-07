"use client";
import { useState } from "react";
import GeomOrnament from "./GeomOrnament";

export default function WirdCard() {
  const [pressed, setPressed] = useState(false);
  const total = 100;
  const done = 67;
  const pct = (done / total) * 100;

  return (
    <section
      className="px-4 section-enter"
      style={{ animationDelay: "0.56s" }}
      aria-label="الورد اليومي"
    >
      {/* Section label */}
      <div className="flex items-center gap-2 mb-3">
        <div className="w-1 h-5 rounded-full" style={{ background: "var(--color-sky)" }} />
        <span className="text-[var(--color-sky)] text-sm font-semibold">الورد اليومي</span>
        <div className="flex-1 h-px" style={{ background: "linear-gradient(to left, transparent, rgba(74,144,196,0.3))" }} />
        <GeomOrnament size={14} color="var(--color-sky)" opacity={0.4} />
      </div>

      <div
        className="relative rounded-2xl overflow-hidden p-4"
        style={{
          background: "linear-gradient(135deg, rgba(27,58,106,0.7) 0%, rgba(19,34,64,0.95) 100%)",
          border: "1px solid rgba(74,144,196,0.2)",
        }}
      >
        {/* ornaments */}
        <GeomOrnament size={100} className="absolute -top-4 -right-4" opacity={0.07} color="var(--color-sky)" />
        <GeomOrnament size={70}  className="absolute -bottom-3 -left-3" opacity={0.07} />

        <div className="relative z-10">
          {/* top */}
          <div className="flex items-start justify-between mb-4">
            <div>
              <p className="text-[var(--color-muted)] text-xs mb-0.5">وردك اليوم</p>
              <p className="text-[var(--color-foreground)] text-lg font-semibold">سورة البقرة</p>
              <p className="text-[var(--color-muted)] text-xs mt-0.5">الآية ١ — ٨٠</p>
            </div>
            <div
              className="flex flex-col items-center justify-center w-14 h-14 rounded-2xl"
              style={{
                background: "rgba(201,168,76,0.12)",
                border: "1px solid rgba(201,168,76,0.25)",
              }}
            >
              <span className="text-2xl font-bold" style={{ color: "var(--color-gold)" }}>
                {Math.round(pct)}
              </span>
              <span className="text-[9px]" style={{ color: "var(--color-muted)" }}>%</span>
            </div>
          </div>

          {/* progress bar */}
          <div className="mb-3">
            <div className="flex justify-between text-[10px] mb-1.5" style={{ color: "var(--color-muted)" }}>
              <span>{done} آية</span>
              <span>من {total} آية</span>
            </div>
            <div className="h-2.5 rounded-full overflow-hidden" style={{ background: "rgba(255,255,255,0.06)" }}>
              <div
                className="h-full rounded-full shimmer-bar transition-all duration-700"
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>

          {/* streaks row */}
          <div className="flex gap-1.5 mb-4" aria-label="أيام الالتزام">
            {Array.from({ length: 7 }, (_, i) => (
              <div
                key={i}
                className="flex-1 h-1.5 rounded-full"
                style={{
                  background: i < 5
                    ? "var(--color-emerald)"
                    : i === 5
                    ? "var(--color-gold)"
                    : "rgba(255,255,255,0.08)",
                }}
                title={`اليوم ${i + 1}`}
              />
            ))}
          </div>
          <p className="text-[10px] mb-4" style={{ color: "var(--color-muted)" }}>
            🔥 سلسلة ٥ أيام متواصلة
          </p>

          {/* CTA */}
          <button
            onPointerDown={() => setPressed(true)}
            onPointerUp={() => setPressed(false)}
            onPointerLeave={() => setPressed(false)}
            className="w-full py-3 rounded-xl font-semibold text-sm transition-transform duration-150"
            style={{
              background: "linear-gradient(135deg, var(--color-sky-dim) 0%, var(--color-sky) 100%)",
              color: "#fff",
              transform: pressed ? "scale(0.97)" : "scale(1)",
              boxShadow: pressed ? "none" : "0 4px 18px rgba(74,144,196,0.35)",
            }}
            aria-label="تابع القراءة"
          >
            تابع القراءة ←
          </button>
        </div>
      </div>
    </section>
  );
}

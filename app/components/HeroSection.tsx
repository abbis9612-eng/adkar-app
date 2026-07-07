"use client";
import { useEffect, useState } from "react";
import StarField from "./StarField";
import GeomOrnament from "./GeomOrnament";

function PrayerCountdown() {
  const [secs, setSecs] = useState(4230); // ~70 min
  useEffect(() => {
    const t = setInterval(() => setSecs((s) => (s > 0 ? s - 1 : 0)), 1000);
    return () => clearInterval(t);
  }, []);
  const h = Math.floor(secs / 3600);
  const m = Math.floor((secs % 3600) / 60);
  const s = secs % 60;
  const pad = (n: number) => String(n).padStart(2, "0");
  return (
    <div className="flex items-center gap-1 justify-center" aria-label="الوقت المتبقي">
      {[pad(h), ":", pad(m), ":", pad(s)].map((v, i) => (
        <span
          key={i}
          className={
            v === ":"
              ? "text-[var(--color-gold-dim)] text-2xl font-light mb-1"
              : "text-3xl font-bold text-[var(--color-gold)] tabular-nums"
          }
          style={{ animation: v !== ":" ? "count-down 0.3s ease" : undefined }}
        >
          {v}
        </span>
      ))}
    </div>
  );
}

const PRAYERS = [
  { name: "الفجر",   time: "04:12", colorVar: "var(--color-fajr)" },
  { name: "الظهر",   time: "12:05", colorVar: "var(--color-dhuhr)", active: true },
  { name: "العصر",   time: "15:41", colorVar: "var(--color-asr)" },
  { name: "المغرب",  time: "18:57", colorVar: "var(--color-maghrib)" },
  { name: "العشاء",  time: "20:22", colorVar: "var(--color-isha)" },
];

export default function HeroSection() {
  const [visible, setVisible] = useState(false);
  useEffect(() => { setTimeout(() => setVisible(true), 80); }, []);

  const activePrayer = PRAYERS.find((p) => p.active) ?? PRAYERS[1];

  return (
    <section
      className="relative w-full overflow-hidden"
      style={{ minHeight: 380 }}
      aria-label="قسم الترويسة"
    >
      <StarField />

      {/* Shimsa corner ornaments */}
      <GeomOrnament size={110} className="absolute -top-4 -right-4" opacity={0.1} />
      <GeomOrnament size={90}  className="absolute -top-4 -left-4"  opacity={0.08} />

      <div className="relative z-10 px-5 pt-10 pb-0">
        {/* Top bar */}
        <div
          className="flex items-center justify-between mb-1 section-enter"
          style={{ animationDelay: "0s" }}
        >
          <button
            className="w-9 h-9 rounded-full glass flex items-center justify-center"
            aria-label="القائمة"
          >
            <svg width="18" height="14" viewBox="0 0 18 14" fill="none" aria-hidden="true">
              <rect x="0" y="0" width="18" height="2" rx="1" fill="var(--color-gold)" />
              <rect x="3" y="6" width="15" height="2" rx="1" fill="var(--color-gold)" />
              <rect x="6" y="12" width="12" height="2" rx="1" fill="var(--color-gold)" />
            </svg>
          </button>

          {/* Date & Hijri */}
          <div className="text-center">
            <p className="text-[var(--color-gold)] text-xs font-medium">
              ٧ محرم ١٤٤٦
            </p>
            <p className="text-[var(--color-muted)] text-[10px]">الاثنين — 7 يوليو 2025</p>
          </div>

          <button
            className="w-9 h-9 rounded-full glass flex items-center justify-center"
            aria-label="الإشعارات"
          >
            <svg width="16" height="18" viewBox="0 0 16 18" fill="none" aria-hidden="true">
              <path
                d="M8 0a1 1 0 011 1v.5A6 6 0 0114 7.5V12l2 2H0l2-2V7.5A6 6 0 017 1.5V1a1 1 0 011-1z"
                fill="var(--color-gold)"
                fillOpacity="0.8"
              />
              <path d="M6 14a2 2 0 004 0H6z" fill="var(--color-gold)" />
            </svg>
          </button>
        </div>

        {/* Basmalah */}
        <div
          className="text-center mt-3 section-enter"
          style={{ animationDelay: "0.1s" }}
        >
          <p
            className="font-serif text-xl leading-loose"
            style={{ color: "var(--color-gold-light)" }}
          >
            بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ
          </p>
        </div>

        {/* Greeting */}
        <div
          className="text-center mt-1 section-enter"
          style={{ animationDelay: "0.18s" }}
        >
          <p className="text-[var(--color-muted)] text-sm">السلام عليكم، أبا عبدالله</p>
        </div>

        {/* Divider */}
        <div className="flex items-center gap-3 my-4 section-enter" style={{ animationDelay: "0.22s" }}>
          <div className="flex-1 h-px" style={{ background: "linear-gradient(to right, transparent, var(--color-gold-dim))" }} />
          <GeomOrnament size={18} opacity={0.55} />
          <div className="flex-1 h-px" style={{ background: "linear-gradient(to left, transparent, var(--color-gold-dim))" }} />
        </div>

        {/* Next prayer card */}
        <div
          className="glass rounded-2xl p-4 glow-border-anim section-enter"
          style={{ animationDelay: "0.28s" }}
        >
          {/* accent bar */}
          <div className="absolute top-0 right-0 left-0 h-1 rounded-t-2xl" style={{ background: `linear-gradient(to right, ${activePrayer.colorVar}, var(--color-gold))` }} />

          <div className="flex items-center justify-between mb-3">
            <div>
              <p className="text-[var(--color-muted)] text-xs mb-0.5">الصلاة القادمة</p>
              <p className="text-[var(--color-foreground)] text-lg font-semibold">
                {activePrayer.name}
              </p>
            </div>
            <div className="text-left">
              <p className="text-[var(--color-muted)] text-xs mb-0.5">الوقت</p>
              <p className="text-[var(--color-gold)] text-lg font-bold">{activePrayer.time}</p>
            </div>
          </div>

          <PrayerCountdown />

          <p className="text-center text-[var(--color-muted)] text-[10px] mt-1">
            المتبقي على الصلاة
          </p>
        </div>
      </div>
    </section>
  );
}

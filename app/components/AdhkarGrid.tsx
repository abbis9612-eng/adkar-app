"use client";
import { useState } from "react";
import GeomOrnament from "./GeomOrnament";

const CATEGORIES = [
  {
    id: "morning",
    label: "أذكار الصباح",
    sub: "٣٦ ذكراً",
    icon: "☀",
    gradient: "linear-gradient(135deg, rgba(201,148,60,0.28) 0%, rgba(27,49,85,0.9) 100%)",
    accent: "var(--color-dhuhr)",
    border: "rgba(201,148,60,0.35)",
    ornColor: "#C9943C",
  },
  {
    id: "evening",
    label: "أذكار المساء",
    sub: "٣٢ ذكراً",
    icon: "🌙",
    gradient: "linear-gradient(135deg, rgba(90,60,120,0.32) 0%, rgba(27,49,85,0.9) 100%)",
    accent: "#8A6AAA",
    border: "rgba(138,106,170,0.35)",
    ornColor: "#8A6AAA",
  },
  {
    id: "prayer",
    label: "أذكار الصلاة",
    sub: "بعد الفريضة",
    icon: "🕌",
    gradient: "linear-gradient(135deg, rgba(58,95,138,0.32) 0%, rgba(27,49,85,0.9) 100%)",
    accent: "var(--color-sky)",
    border: "rgba(74,144,196,0.35)",
    ornColor: "#4A90C4",
  },
  {
    id: "sleep",
    label: "أذكار النوم",
    sub: "١٦ ذكراً",
    icon: "✨",
    gradient: "linear-gradient(135deg, rgba(58,58,122,0.32) 0%, rgba(27,49,85,0.9) 100%)",
    accent: "var(--color-isha)",
    border: "rgba(58,58,122,0.5)",
    ornColor: "#5A5ABB",
  },
  {
    id: "istighfar",
    label: "الاستغفار",
    sub: "٨ أذكار",
    icon: "💚",
    gradient: "linear-gradient(135deg, rgba(45,138,106,0.28) 0%, rgba(27,49,85,0.9) 100%)",
    accent: "var(--color-emerald)",
    border: "rgba(45,138,106,0.35)",
    ornColor: "#2D8A6A",
  },
  {
    id: "quran",
    label: "أدعية قرآنية",
    sub: "آيات مختارة",
    icon: "📖",
    gradient: "linear-gradient(135deg, rgba(201,168,76,0.18) 0%, rgba(27,49,85,0.9) 100%)",
    accent: "var(--color-gold)",
    border: "rgba(201,168,76,0.3)",
    ornColor: "#C9A84C",
  },
];

export default function AdhkarGrid() {
  const [pressed, setPressed] = useState<string | null>(null);

  return (
    <section
      className="px-4 section-enter"
      style={{ animationDelay: "0.48s" }}
      aria-label="الأذكار"
    >
      {/* Section label */}
      <div className="flex items-center gap-2 mb-3">
        <div className="w-1 h-5 rounded-full" style={{ background: "var(--color-emerald)" }} />
        <span className="text-[var(--color-emerald-light)] text-sm font-semibold">الأذكار</span>
        <div className="flex-1 h-px" style={{ background: "linear-gradient(to left, transparent, rgba(45,138,106,0.4))" }} />
        <GeomOrnament size={14} color="var(--color-emerald)" opacity={0.4} />
      </div>

      <div className="grid grid-cols-2 gap-3">
        {CATEGORIES.map((cat, i) => (
          <button
            key={cat.id}
            onPointerDown={() => setPressed(cat.id)}
            onPointerUp={() => setPressed(null)}
            onPointerLeave={() => setPressed(null)}
            className="relative rounded-2xl p-4 text-right overflow-hidden transition-transform duration-150 section-enter"
            style={{
              background: cat.gradient,
              border: `1px solid ${cat.border}`,
              transform: pressed === cat.id ? "scale(0.96)" : "scale(1)",
              animationDelay: `${0.48 + i * 0.06}s`,
            }}
            aria-label={cat.label}
          >
            {/* bg ornament */}
            <GeomOrnament
              size={70}
              color={cat.ornColor}
              className="absolute -bottom-3 -left-3"
              opacity={0.12}
            />
            {/* icon chip */}
            <div
              className="inline-flex items-center justify-center w-9 h-9 rounded-xl text-lg mb-2"
              style={{
                background: `${cat.border}`,
                border: `1px solid ${cat.border}`,
              }}
              aria-hidden="true"
            >
              {cat.icon}
            </div>
            <p
              className="text-sm font-semibold leading-snug"
              style={{ color: "var(--color-foreground)" }}
            >
              {cat.label}
            </p>
            <p className="text-[10px] mt-0.5" style={{ color: "var(--color-muted)" }}>
              {cat.sub}
            </p>
            {/* accent bottom bar */}
            <div
              className="absolute bottom-0 right-0 left-0 h-0.5 rounded-b-2xl"
              style={{ background: cat.accent }}
            />
          </button>
        ))}
      </div>
    </section>
  );
}

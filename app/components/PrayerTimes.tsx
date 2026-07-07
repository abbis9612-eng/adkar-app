"use client";
import GeomOrnament from "./GeomOrnament";

const PRAYERS = [
  { name: "الفجر",   time: "04:12", icon: "🌄", color: "var(--color-fajr)",    done: true  },
  { name: "الشروق",  time: "05:43", icon: "☀",  color: "#A0704A",             done: true  },
  { name: "الظهر",   time: "12:05", icon: "🌞", color: "var(--color-dhuhr)",   active: true },
  { name: "العصر",   time: "15:41", icon: "🌤",  color: "var(--color-asr)" },
  { name: "المغرب",  time: "18:57", icon: "🌇", color: "var(--color-maghrib)" },
  { name: "العشاء",  time: "20:22", icon: "🌙", color: "var(--color-isha)" },
];

export default function PrayerTimes() {
  return (
    <section
      className="px-4 section-enter"
      style={{ animationDelay: "0.64s" }}
      aria-label="مواقيت الصلاة"
    >
      {/* Section label */}
      <div className="flex items-center gap-2 mb-3">
        <div className="w-1 h-5 rounded-full" style={{ background: "var(--color-maghrib)" }} />
        <span className="text-sm font-semibold" style={{ color: "#CC88AA" }}>
          مواقيت الصلاة
        </span>
        <div className="flex-1 h-px" style={{ background: "linear-gradient(to left, transparent, rgba(138,74,106,0.35))" }} />
        <GeomOrnament size={14} color="#CC88AA" opacity={0.4} />
      </div>

      <div
        className="rounded-2xl overflow-hidden"
        style={{
          background: "rgba(19,34,64,0.8)",
          border: "1px solid var(--color-border)",
        }}
      >
        {PRAYERS.map((p, i) => (
          <div
            key={p.name}
            className="relative flex items-center px-4 py-3"
            style={{
              borderBottom: i < PRAYERS.length - 1 ? "1px solid rgba(255,255,255,0.04)" : "none",
              background: p.active
                ? `linear-gradient(90deg, rgba(${hexToRgb(p.color)},0.18) 0%, transparent 100%)`
                : undefined,
            }}
          >
            {/* active indicator bar */}
            {p.active && (
              <div
                className="absolute right-0 top-0 bottom-0 w-1 rounded-l-full"
                style={{ background: p.color }}
              />
            )}

            {/* icon */}
            <span
              className="w-8 h-8 rounded-xl flex items-center justify-center text-base ml-3"
              style={{
                background: p.active
                  ? `rgba(${hexToRgb(p.color)},0.2)`
                  : "rgba(255,255,255,0.05)",
                border: p.active ? `1px solid ${p.color}` : "none",
              }}
              aria-hidden="true"
            >
              {p.icon}
            </span>

            {/* name */}
            <div className="flex-1">
              <span
                className="text-sm font-medium"
                style={{
                  color: p.active
                    ? "var(--color-foreground)"
                    : p.done
                    ? "var(--color-muted)"
                    : "var(--color-foreground)",
                  opacity: p.done && !p.active ? 0.6 : 1,
                }}
              >
                {p.name}
              </span>
              {p.active && (
                <div className="flex items-center gap-1 mt-0.5">
                  <span
                    className="pulse-ring inline-block w-1.5 h-1.5 rounded-full"
                    style={{ background: p.color }}
                    aria-hidden="true"
                  />
                  <span className="text-[10px]" style={{ color: p.color }}>جارية الآن</span>
                </div>
              )}
            </div>

            {/* time */}
            <span
              className="text-sm font-bold tabular-nums"
              style={{
                color: p.active ? p.color : p.done ? "var(--color-muted)" : "var(--color-foreground)",
                opacity: p.done && !p.active ? 0.55 : 1,
              }}
            >
              {p.time}
            </span>

            {/* done check */}
            {p.done && !p.active && (
              <span
                className="mr-2 w-4 h-4 rounded-full flex items-center justify-center text-[9px]"
                style={{ background: "var(--color-emerald)", color: "#fff" }}
                aria-label="صُليت"
              >
                ✓
              </span>
            )}
          </div>
        ))}
      </div>
    </section>
  );
}

/** helper — convert CSS var or hex color to rgb for rgba usage */
function hexToRgb(color: string): string {
  // fallback for CSS vars
  const map: Record<string, string> = {
    "var(--color-fajr)":    "58,95,138",
    "var(--color-dhuhr)":   "201,148,60",
    "var(--color-asr)":     "74,138,106",
    "var(--color-maghrib)": "138,74,106",
    "var(--color-isha)":    "58,58,122",
  };
  return map[color] ?? "201,168,76";
}

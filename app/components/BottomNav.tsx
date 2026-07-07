"use client";
import { useState } from "react";

const TABS = [
  {
    id: "home",
    label: "الرئيسية",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M3 12L12 3l9 9" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M5 10v9a1 1 0 001 1h4v-5h4v5h4a1 1 0 001-1v-9" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
  },
  {
    id: "adhkar",
    label: "الأذكار",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.8" />
        <path d="M12 7v5l3 3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      </svg>
    ),
  },
  {
    id: "quran",
    label: "القرآن",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M4 19.5A2.5 2.5 0 016.5 17H20" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        <path d="M4 4.5A2.5 2.5 0 016.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15z" stroke="currentColor" strokeWidth="1.8" />
      </svg>
    ),
  },
  {
    id: "prayer",
    label: "الصلاة",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M12 2C9.24 2 7 4.24 7 7c0 3.53 4 8.5 5 9.93C13 15.5 17 10.53 17 7c0-2.76-2.24-5-5-5z" stroke="currentColor" strokeWidth="1.8" />
        <circle cx="12" cy="7" r="2" stroke="currentColor" strokeWidth="1.6" />
        <path d="M5 22h14" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      </svg>
    ),
  },
  {
    id: "profile",
    label: "حسابي",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="12" cy="8" r="4" stroke="currentColor" strokeWidth="1.8" />
        <path d="M4 20c0-4 3.58-7 8-7s8 3 8 7" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
      </svg>
    ),
  },
];

export default function BottomNav() {
  const [active, setActive] = useState("home");

  return (
    <nav
      className="sticky bottom-0 z-50 glass border-t"
      style={{ borderColor: "var(--color-border)" }}
      aria-label="شريط التنقل"
    >
      <div className="flex items-center justify-around py-2 px-2">
        {TABS.map((tab) => {
          const isActive = tab.id === active;
          return (
            <button
              key={tab.id}
              onClick={() => setActive(tab.id)}
              className="flex flex-col items-center gap-1 px-3 py-1.5 rounded-xl transition-all duration-200"
              style={{
                color: isActive ? "var(--color-gold)" : "var(--color-muted)",
                background: isActive ? "rgba(201,168,76,0.1)" : "transparent",
              }}
              aria-label={tab.label}
              aria-current={isActive ? "page" : undefined}
            >
              {tab.icon}
              <span className="text-[10px] font-medium">{tab.label}</span>
              {isActive && (
                <span
                  className="w-1 h-1 rounded-full"
                  style={{ background: "var(--color-gold)" }}
                  aria-hidden="true"
                />
              )}
            </button>
          );
        })}
      </div>
    </nav>
  );
}

import HeroSection  from "./components/HeroSection";
import NowCard      from "./components/NowCard";
import AdhkarGrid   from "./components/AdhkarGrid";
import WirdCard     from "./components/WirdCard";
import PrayerTimes  from "./components/PrayerTimes";
import BottomNav    from "./components/BottomNav";

export default function Home() {
  return (
    <main className="min-h-screen flex justify-center" style={{ background: "var(--color-background)" }}>
      {/* Phone frame wrapper — centres content on desktop */}
      <div
        className="relative w-full flex flex-col"
        style={{ maxWidth: 430 }}
      >
        {/* Scrollable content */}
        <div className="flex-1 overflow-y-auto pb-24">
          <HeroSection />

          <div className="flex flex-col gap-5 pt-5">
            <NowCard />
            <AdhkarGrid />
            <WirdCard />
            <PrayerTimes />

            {/* Bottom spacer */}
            <div className="h-4" />
          </div>
        </div>

        {/* Bottom nav */}
        <BottomNav />
      </div>
    </main>
  );
}

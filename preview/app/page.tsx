import HeroSection  from "./components/HeroSection";
import NowCard      from "./components/NowCard";
import AdhkarGrid   from "./components/AdhkarGrid";
import WirdCard     from "./components/WirdCard";
import PrayerTimes  from "./components/PrayerTimes";
import BottomNav    from "./components/BottomNav";

export default function Home() {
  return (
    <main
      className="flex justify-center"
      style={{ background: "var(--color-background)", minHeight: "100dvh" }}
    >
      {/* Phone frame wrapper */}
      <div
        className="relative w-full flex flex-col"
        style={{ maxWidth: 430, minHeight: "100dvh" }}
      >
        {/* Scrollable content — leaves space for fixed bottom nav */}
        <div className="flex-1" style={{ paddingBottom: 72, overflowY: "auto" }}>
          <HeroSection />
          <div className="flex flex-col gap-5 pt-5">
            <NowCard />
            <AdhkarGrid />
            <WirdCard />
            <PrayerTimes />
            <div className="h-4" />
          </div>
        </div>

        {/* Fixed bottom nav */}
        <div className="fixed bottom-0 left-0 right-0 z-50 flex justify-center">
          <div className="w-full" style={{ maxWidth: 430 }}>
            <BottomNav />
          </div>
        </div>
      </div>
    </main>
  );
}

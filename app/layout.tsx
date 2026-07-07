import type { Metadata } from "next";
import { Noto_Sans_Arabic, Amiri } from "next/font/google";
import "./globals.css";

const notoSansArabic = Noto_Sans_Arabic({
  subsets: ["arabic"],
  variable: "--font-sans",
  weight: ["300", "400", "500", "600", "700"],
});

const amiri = Amiri({
  subsets: ["arabic"],
  variable: "--font-serif",
  weight: ["400", "700"],
});

export const metadata: Metadata = {
  title: "رفيق الذكر — معاينة الصفحة الرئيسية",
  description: "معاينة تصميم تطبيق رفيق الذكر",
  themeColor: "#0B1A2E",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ar" dir="rtl" className="bg-[var(--color-background)]">
      <body className={`${notoSansArabic.variable} ${amiri.variable} font-sans`}>
        {children}
      </body>
    </html>
  );
}

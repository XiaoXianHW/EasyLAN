import type { Metadata } from "next";
import "./globals.css";
import { TopNav } from "@/components/top-nav";
import { Footer } from "@/components/footer";

export const metadata: Metadata = {
  title: "EasyLAN — Minecraft 联机房平台",
  description:
    "EasyLAN 是为 Minecraft 玩家打造的房间撮合平台：检测 NAT 类型、生成房间码、支持 P2P / FRP / IPv6 多种联机模式。",
  applicationName: "EasyLAN",
  icons: { icon: "/favicon.svg" }
};

export default function RootLayout({
  children
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className="bg-canvas text-ink">
      <body className="bg-canvas text-ink antialiased">
        <TopNav />
        <main className="mx-auto max-w-6xl px-6 pb-24 pt-10 sm:px-10">{children}</main>
        <Footer />
      </body>
    </html>
  );
}

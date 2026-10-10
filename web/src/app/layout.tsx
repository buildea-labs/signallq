import type { Metadata, Viewport } from "next";
import "../index.css";
import { SiteNav } from "../components/SiteNav";
import { SiteFooter } from "../components/SiteFooter";
import { ThemeScript } from "../components/ThemeScript";
import { TelemetryInit } from "../components/TelemetryInit";
import { SITE_ORIGIN } from "../lib/routeMetadata";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_ORIGIN),
  title: "SignallQ — o app que descobre por que sua internet está ruim",
  description: "O app que não para no número: aponta causas prováveis da sua internet ruim. Baixe na Play Store.",
  icons: {
    icon: [
      {
        url: "/assets/signallq-favicon-web-light-bg.png",
        sizes: "1024x1024",
        type: "image/png",
        media: "(prefers-color-scheme: light)",
      },
      {
        url: "/assets/signallq-favicon-web-dark-bg.png",
        sizes: "1024x1024",
        type: "image/png",
        media: "(prefers-color-scheme: dark)",
      },
    ],
    apple: [{ url: "/assets/signallq-icon-512-play-store-dark.png", sizes: "1024x1024", type: "image/png" }],
  },
  appleWebApp: {
    capable: true,
    title: "SignallQ",
    statusBarStyle: "black-translucent",
  },
};

export const viewport: Viewport = {
  // Uma cor por esquema: antes um único #131217 pintava a barra do navegador
  // também no modo claro, onde o fundo é branco.
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#FFFFFF" },
    { media: "(prefers-color-scheme: dark)", color: "#000000" },
  ],
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
  // maximumScale e userScalable removidos intencionalmente para permitir zoom (Acessibilidade)
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" className="antialiased" data-sq-theme="system" suppressHydrationWarning>
      <head>
        {/* eslint-disable-next-line @next/next/no-css-tags */}
        <link rel="stylesheet" href="/assets/google-sans-flex.css" />
        {/* eslint-disable-next-line @next/next/no-css-tags */}
        <link rel="stylesheet" href="/_ds/signallq-design-system-2d25d7a1-31b2-4ac3-881f-72dbc8f35a29/_ds_bundle.css" />
        {/* eslint-disable-next-line @next/next/no-css-tags */}
        <link rel="stylesheet" href="/_ds/signallq-design-system-2d25d7a1-31b2-4ac3-881f-72dbc8f35a29/styles.css" />
        <ThemeScript />
      </head>
      <body className="bg-[color:var(--bg-primary)] text-[color:var(--text-primary)]">
        <TelemetryInit />
        {/* SiteNav + miolo em min-h-screen própria (não a <body> inteira, que
            também engloba o SiteFooter abaixo) — garante que o rodapé nunca
            apareça na primeira vista, mesmo em telas com pouco conteúdo
            (ex.: 404): é preciso rolar pra passar da altura de uma
            viewport antes de alcançá-lo. SiteNav/SiteFooter vivem aqui, no
            layout raiz, pra persistir entre navegações (guia §1) — mover
            página não deve remontar o header/rodapé (achado 01/08/2026,
            "topbar sambando" ao trocar de rota). */}
        <div className="flex min-h-screen w-full flex-col">
          <SiteNav />
          {/* `flex-1` aqui é o que dá altura ao miolo: sem isso, o
              `align="center"` do `PageShell` não tinha espaço para centralizar
              e as etapas curtas do fluxo de velocidade (formação, medição,
              falha) ficavam ancoradas no topo com um vazio embaixo. */}
          <main className="flex w-full flex-1 flex-col">
            {children}
          </main>
        </div>
        <SiteFooter />
      </body>
    </html>
  );
}

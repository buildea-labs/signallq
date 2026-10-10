"use client";
import { useRevealOnScroll } from './useRevealOnScroll'
import { useAppLanding } from './useAppLanding'
import {
  AppLandingHero,
  AppLandingFeatures,
  AppLandingGallery,
  AppLandingCTA,
  APP_DIFERENCIAIS_ID,
} from './AppLandingComponents'
import { HowItWorksSteps } from './HowItWorksSteps'
import { ResultOrder } from './ResultOrder'
import { TrustPromises } from './TrustPromises'
import { LandingFaq } from './LandingFaq'

const HOME_JSON_LD = {
  '@context': 'https://schema.org',
  '@type': 'SoftwareApplication',
  name: 'SignallQ',
  operatingSystem: 'Android',
  applicationCategory: 'UtilitiesApplication',
  description: 'Diagnóstico de conectividade: analisa Wi-Fi, velocidade e DNS, explica o problema e orienta a solução.',
  offers: { '@type': 'Offer', price: '0', priceCurrency: 'BRL' },
  inLanguage: 'pt-BR',
  publisher: { '@type': 'Organization', name: '7Agents Tecnologia', url: 'https://signallq.com' },
}

export function AppLandingClient() {
  const { baixarNaPlayStore } = useAppLanding()

  useRevealOnScroll()

  function verDiferenciais() {
    const el = document.getElementById(APP_DIFERENCIAIS_ID)
    if (!el) return
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    el.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' })
    // Move o foco de teclado junto com o scroll (padrão de skip-link): quem
    // ativou a seta via teclado continua a navegação a partir da seção
    // revelada, em vez de ficar com o foco "perdido" no botão que já saiu
    // da viewport.
    el.focus({ preventScroll: true })
  }

  return (
    <div className="relative flex w-full flex-col">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(HOME_JSON_LD) }} />
      <AppLandingHero
        onBaixar={baixarNaPlayStore}
        onVerDiferenciais={verDiferenciais}
      />

      <div className="w-full box-border flex justify-center pb-4 px-[var(--safe-x)]">
        <div className="w-full max-w-[1080px] flex flex-col gap-[56px]">
          <AppLandingFeatures />
          <HowItWorksSteps />
          <ResultOrder />
          <AppLandingGallery />
          <TrustPromises />
          <LandingFaq />
          <AppLandingCTA onBaixar={baixarNaPlayStore} />
        </div>
      </div>
    </div>
  )
}

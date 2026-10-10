"use client";

import { Banda } from '@/components/Banda'
import { ReadingLayout } from '@/components/institutional/InstitutionalFoundation'
import { trackFeatureUsed } from '@/lib/telemetry'
import { SITE_ORIGIN } from '@/lib/routeMetadata'
import { useSpeedTestEngine } from './useSpeedTestEngine'
import { SpeedTestIdle } from './SpeedTestIdle'
import { SpeedTestRunning } from './SpeedTestRunning'
import { SpeedTestResult } from './SpeedTestResult'
import { SpeedTestError } from './SpeedTestError'

const JSON_LD = {
  '@context': 'https://schema.org',
  '@type': 'WebApplication',
  name: 'Teste de velocidade SignallQ',
  url: `${SITE_ORIGIN}/teste-de-velocidade`,
  applicationCategory: 'UtilitiesApplication',
  operatingSystem: 'Any',
  inLanguage: 'pt-BR',
  offers: { '@type': 'Offer', price: '0', priceCurrency: 'BRL' },
}

export const FEATURE_TESTE_VELOCIDADE_INICIADO = 'teste_velocidade_iniciado'

export function SpeedTestContent() {
  const { state, start, cancel } = useSpeedTestEngine()

  function handleStart() {
    trackFeatureUsed(FEATURE_TESTE_VELOCIDADE_INICIADO)
    start()
  }

  return (
    <Banda className="py-8 md:py-12 lg:py-16">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(JSON_LD) }} />
      <ReadingLayout className="min-h-[480px]">
        {state.phase === 'idle' && <SpeedTestIdle onStart={handleStart} />}
        {state.phase === 'running' && <SpeedTestRunning state={state} onCancel={cancel} />}
        {state.phase === 'done' && <SpeedTestResult state={state} onRetry={handleStart} />}
        {state.phase === 'error' && <SpeedTestError onRetry={handleStart} />}
      </ReadingLayout>
    </Banda>
  )
}

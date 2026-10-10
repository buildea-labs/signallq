"use client";

import { useCallback, useRef, useState } from 'react'
import { SPEEDTEST_DOWNLOAD_URL, SPEEDTEST_UPLOAD_URL } from '@/lib/config'

export type SpeedTestPhase = 'idle' | 'running' | 'done' | 'error'
export type SpeedTestStage = 'ping' | 'down' | 'up' | ''

export interface SpeedTestState {
  phase: SpeedTestPhase
  stage: SpeedTestStage
  live: number
  pct: number
  ping: number | null
  download: number | null
  upload: number | null
}

const INITIAL_STATE: SpeedTestState = { phase: 'idle', stage: '', live: 0, pct: 0, ping: null, download: null, upload: null }

// Máquina de estados da medição (ping → download → upload). Só mede e expõe
// estado bruto — formatação/veredito ficam em speedTestVerdict.ts, nunca
// aqui, pra manter esta peça com uma única responsabilidade (guardrail de
// arquitetura do repo).
export function useSpeedTestEngine() {
  const [state, setState] = useState<SpeedTestState>(INITIAL_STATE)
  const runId = useRef(0)

  const start = useCallback(async () => {
    const id = ++runId.current
    const current = () => runId.current === id
    setState({ phase: 'running', stage: 'ping', live: 0, pct: 3, ping: null, download: null, upload: null })

    try {
      const pingSamples: number[] = []
      for (let i = 0; i < 6; i++) {
        const started = performance.now()
        await fetch(`${SPEEDTEST_DOWNLOAD_URL}?bytes=0&r=${Math.random()}`, { cache: 'no-store' })
        pingSamples.push(performance.now() - started)
        if (!current()) return
        setState((s) => ({ ...s, live: Math.round(Math.min(...pingSamples)), pct: 3 + i * 2 }))
      }
      const ping = Math.round(Math.min(...pingSamples))
      setState((s) => ({ ...s, ping, stage: 'down', live: 0, pct: 15 }))

      const downloadStart = performance.now()
      let bytes = 0
      let download = 0
      for (const size of [2e6, 8e6, 25e6]) {
        const res = await fetch(`${SPEEDTEST_DOWNLOAD_URL}?bytes=${size}&r=${Math.random()}`, { cache: 'no-store' })
        const reader = res.body?.getReader()
        if (!reader) break
        for (;;) {
          const { done, value } = await reader.read()
          if (done) break
          bytes += value.length
          const elapsed = (performance.now() - downloadStart) / 1000
          download = (bytes * 8) / elapsed / 1e6
          if (!current()) { reader.cancel(); return }
          setState((s) => ({ ...s, live: download, pct: 15 + Math.min(45, elapsed * 6) }))
        }
        if ((performance.now() - downloadStart) / 1000 > 6) break
      }
      setState((s) => ({ ...s, download, stage: 'up', live: 0, pct: 62 }))

      const uploadBlob = new Uint8Array(2e6)
      const uploadStart = performance.now()
      let uploadedBytes = 0
      let upload = 0
      for (let i = 0; i < 6; i++) {
        await fetch(SPEEDTEST_UPLOAD_URL, { method: 'POST', body: uploadBlob, cache: 'no-store' })
        uploadedBytes += uploadBlob.length
        upload = (uploadedBytes * 8) / ((performance.now() - uploadStart) / 1000) / 1e6
        if (!current()) return
        setState((s) => ({ ...s, live: upload, pct: 62 + (i + 1) * 6 }))
        if ((performance.now() - uploadStart) / 1000 > 6) break
      }
      setState((s) => ({ ...s, upload, phase: 'done', pct: 100 }))
    } catch {
      if (current()) setState((s) => ({ ...s, phase: 'error' }))
    }
  }, [])

  const cancel = useCallback(() => {
    runId.current++
    setState(INITIAL_STATE)
  }, [])

  return { state, start, cancel }
}

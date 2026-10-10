import { formatMetric } from './speedTestVerdict'
import type { SpeedTestState } from './useSpeedTestEngine'

const STAGE_LABELS: Record<string, string> = {
  ping: 'Medindo latência…',
  down: 'Medindo download…',
  up: 'Medindo upload…',
}

export function SpeedTestRunning({ state, onCancel }: { state: SpeedTestState; onCancel: () => void }) {
  const liveUnit = state.stage === 'ping' ? 'ms' : 'Mbps'
  const liveValue = state.stage === 'ping' ? String(Math.round(state.live)) : formatMetric(state.live)

  return (
    <div className="text-center">
      <div className="body-medium" style={{ color: 'var(--text-secondary)' }}>{STAGE_LABELS[state.stage] ?? ''}</div>
      <div className="mt-5 flex items-baseline justify-center gap-3">
        <span className="font-bold tabular-nums text-[clamp(72px,16vw,128px)] leading-none">{liveValue}</span>
        <span className="title-medium" style={{ color: 'var(--text-secondary)' }}>{liveUnit}</span>
      </div>
      <div className="mx-auto mt-10 h-[6px] w-full max-w-[420px] overflow-hidden rounded-full" style={{ background: 'var(--bg-secondary)' }}>
        <div
          className="h-full rounded-full transition-[width] duration-300"
          style={{ width: `${Math.round(state.pct)}%`, background: 'var(--accent)' }}
        />
      </div>
      <div className="mt-6 flex justify-center gap-8 text-[14px]" style={{ color: 'var(--text-secondary)' }}>
        <span>Latência <b style={{ color: 'var(--text-primary)' }}>{state.ping == null ? '—' : `${state.ping} ms`}</b></span>
        <span>Download <b style={{ color: 'var(--text-primary)' }}>{state.download == null ? '—' : `${formatMetric(state.download)} Mbps`}</b></span>
        <span>Upload <b style={{ color: 'var(--text-primary)' }}>{state.upload == null ? '—' : `${formatMetric(state.upload)} Mbps`}</b></span>
      </div>
      <button
        type="button"
        onClick={onCancel}
        className="label-large mt-10 rounded-[var(--radius-button)] px-7 py-3"
        style={{ background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}
      >
        Cancelar
      </button>
    </div>
  )
}

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
      <div className="text-[16px] font-medium text-[#49454F]">{STAGE_LABELS[state.stage] ?? ''}</div>
      <div className="mt-5 flex items-baseline justify-center gap-[10px]">
        <span className="text-[clamp(88px,18vw,144px)] font-bold leading-none tracking-[-4px] tabular-nums">{liveValue}</span>
        <span className="text-[22px] font-medium text-[#49454F]">{liveUnit}</span>
      </div>
      <div className="mx-auto mt-11 h-[6px] max-w-[420px] overflow-hidden rounded-full bg-[#F3EEFA]">
        <div
          className="h-full rounded-full bg-[#5B21D6] transition-[width] duration-300"
          style={{ width: `${Math.round(state.pct)}%` }}
        />
      </div>
      <div className="mx-auto mt-7 flex justify-center gap-8 text-[14px] text-[#49454F]">
        <span>Latência <b className="text-[#1C1B1F]">{state.ping == null ? '—' : `${state.ping} ms`}</b></span>
        <span>Download <b className="text-[#1C1B1F]">{state.download == null ? '—' : `${formatMetric(state.download)} Mbps`}</b></span>
        <span>Upload <b className="text-[#1C1B1F]">{state.upload == null ? '—' : `${formatMetric(state.upload)} Mbps`}</b></span>
      </div>
      <button
        type="button"
        onClick={onCancel}
        className="mt-10 cursor-pointer rounded-full border-0 bg-[#F3EEFA] px-7 py-[13px] text-[15px] font-medium text-[#210A5C]"
      >
        Cancelar
      </button>
    </div>
  )
}

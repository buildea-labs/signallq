import Link from 'next/link'
import { formatMetric, getUsageLevels, getVerdict } from './speedTestVerdict'
import type { SpeedTestState } from './useSpeedTestEngine'

export function SpeedTestResult({ state, onRetry }: { state: SpeedTestState; onRetry: () => void }) {
  const verdict = getVerdict(state.download)
  const usage = getUsageLevels(state.download, state.upload, state.ping)

  return (
    <div>
      <div className="body-medium" style={{ color: 'var(--text-secondary)' }}>Resultado</div>
      <h1 className="headline-large m-0 mt-3 text-balance">{verdict.title}</h1>
      <p className="body-medium mt-4 text-pretty" style={{ color: 'var(--text-secondary)' }}>{verdict.subtitle}</p>

      <div className="mt-10 grid grid-cols-3 gap-6 border-t pt-7" style={{ borderColor: 'var(--border)' }}>
        <div>
          <div className="label-large" style={{ color: 'var(--text-secondary)' }}>Download</div>
          <div className="mt-1 font-bold text-[32px] leading-none">{formatMetric(state.download)}<span className="label-large" style={{ color: 'var(--text-secondary)' }}> Mbps</span></div>
          <div className="body-small mt-2" style={{ color: 'var(--text-secondary)' }}>Para baixar vídeos, páginas e arquivos.</div>
        </div>
        <div>
          <div className="label-large" style={{ color: 'var(--text-secondary)' }}>Upload</div>
          <div className="mt-1 font-bold text-[32px] leading-none">{formatMetric(state.upload)}<span className="label-large" style={{ color: 'var(--text-secondary)' }}> Mbps</span></div>
          <div className="body-small mt-2" style={{ color: 'var(--text-secondary)' }}>Para enviar fotos e fazer videochamadas.</div>
        </div>
        <div>
          <div className="label-large" style={{ color: 'var(--text-secondary)' }}>Latência</div>
          <div className="mt-1 font-bold text-[32px] leading-none">{state.ping ?? '—'}<span className="label-large" style={{ color: 'var(--text-secondary)' }}> ms</span></div>
          <div className="body-small mt-2" style={{ color: 'var(--text-secondary)' }}>Quanto menor, melhor para jogos e chamadas.</div>
        </div>
      </div>

      <div className="mt-10">
        <div className="label-large mb-1" style={{ color: 'var(--text-secondary)' }}>Com essa conexão</div>
        {usage.map((u) => (
          <div key={u.n} className="flex justify-between gap-4 border-b py-3 body-medium" style={{ borderColor: 'var(--bg-secondary)' }}>
            <span>{u.n}</span>
            <b style={{ color: u.c }}>{u.s}</b>
          </div>
        ))}
      </div>

      <div className="mt-10 flex flex-wrap items-center justify-between gap-4 rounded-[var(--radius-card)] px-6 py-6" style={{ background: 'var(--bg-secondary)' }}>
        <div className="max-w-[24em]">
          <div className="title-medium m-0">Algo não parece certo?</div>
          <div className="body-small mt-1" style={{ color: 'var(--text-secondary)' }}>O app descobre a causa e diz o que fazer.</div>
        </div>
        <Link
          href="/#baixar"
          className="label-large flex h-10 items-center justify-center rounded-[var(--radius-button)] px-5 no-underline"
          style={{ background: 'var(--accent)', color: 'var(--on-accent)' }}
        >
          Diagnosticar no app
        </Link>
      </div>

      <button type="button" onClick={onRetry} className="label-large mt-7 p-0" style={{ background: 'none', border: 0, color: 'var(--accent)' }}>
        Testar de novo
      </button>
    </div>
  )
}

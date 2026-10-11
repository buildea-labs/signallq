import Link from 'next/link'
import { formatMetric, getUsageLevels, getVerdict } from './speedTestVerdict'
import type { SpeedTestState } from './useSpeedTestEngine'

function Metric({ label, value, unit, text }: { label: string; value: string; unit: string; text: string }) {
  return (
    <div>
      <div className="text-[13px] font-medium text-[#49454F]">{label}</div>
      <div className="mt-[6px] text-[36px] font-bold tracking-[-1px]">
        {value}
        <span className="text-[15px] font-medium tracking-normal text-[#49454F]"> {unit}</span>
      </div>
      <div className="mt-[6px] text-[14px] leading-5 text-[#49454F]">{text}</div>
    </div>
  )
}

export function SpeedTestResult({ state, onRetry }: { state: SpeedTestState; onRetry: () => void }) {
  const verdict = getVerdict(state.download)
  const usage = getUsageLevels(state.download, state.upload, state.ping)

  return (
    <div>
      <div className="text-[14px] font-medium text-[#49454F]">Resultado</div>
      <h1 className="m-0 mt-3 text-balance text-[clamp(34px,4.6vw,48px)] font-bold leading-[1.08] tracking-[-1.2px]">{verdict.title}</h1>
      <p className="mt-4 text-pretty text-[18px] leading-7 text-[#49454F]">{verdict.subtitle}</p>

      <div className="mt-10 grid grid-cols-3 gap-6 border-t border-[#E7E0EC] pt-7">
        <Metric label="Download" value={formatMetric(state.download)} unit="Mbps" text="Velocidade para baixar vídeos, páginas e arquivos." />
        <Metric label="Upload" value={formatMetric(state.upload)} unit="Mbps" text="Velocidade para enviar fotos e fazer videochamadas." />
        <Metric label="Latência" value={state.ping == null ? '—' : String(state.ping)} unit="ms" text="Tempo de resposta. Quanto menor, melhor para jogos e chamadas." />
      </div>

      <div className="mt-10">
        <div className="mb-1 text-[14px] font-semibold text-[#49454F]">Com essa conexão</div>
        {usage.map((u) => (
          <div key={u.n} className="flex justify-between gap-4 border-b border-[#F3EEFA] py-[14px] text-[16px]">
            <span>{u.n}</span>
            <b style={{ color: u.c }}>{u.s}</b>
          </div>
        ))}
      </div>

      <div className="mt-10 flex flex-wrap items-center justify-between gap-x-6 gap-y-5 rounded-3xl bg-[#EAE0FF] p-7">
        <div className="max-w-[24em]">
          <div className="text-[20px] font-semibold text-[#210A5C]">Algo não parece certo?</div>
          <div className="mt-[6px] text-[15px] leading-[22px] text-[#210A5C]">O app descobre a causa e diz o que fazer.</div>
        </div>
        <Link href="/#baixar" className="rounded-full bg-[#5B21D6] px-[26px] py-[14px] text-[15px] font-medium text-white no-underline">
          Diagnosticar no app
        </Link>
      </div>

      <button type="button" onClick={onRetry} className="mt-7 cursor-pointer border-0 bg-transparent px-0 py-2 text-[16px] font-semibold text-[#5B21D6]">
        Testar de novo
      </button>
    </div>
  )
}

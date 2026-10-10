import type { ReactNode } from 'react'

export type LegalSection = { t: string; ps: ReactNode[] }

type LegalPageProps = {
  title: string
  updated: string
  intro?: string
  summary?: { title: string; text: ReactNode }
  sections: LegalSection[]
  sectionGap?: 'mt-11' | 'mt-12'
}

// Layout de Privacidade/Termos do protótipo (SignallQ Privacidade/Termos.dc.html).
export function LegalPage({ title, updated, intro, summary, sections, sectionGap = 'mt-11' }: LegalPageProps) {
  return (
    <article className="mx-auto w-full max-w-[768px] bg-white px-6 pb-[120px] pt-[88px] font-sans leading-[normal] text-[#1C1B1F]">
      <h1 className="m-0 text-[clamp(36px,4.6vw,52px)] font-bold leading-[1.05] tracking-[-1.2px]">{title}</h1>
      <p className="mt-4 text-[14px] text-[#49454F]">{updated}</p>
      {intro && <p className="mt-7 text-pretty text-[18px] leading-7 text-[#49454F]">{intro}</p>}
      {summary && (
        <div className="mt-10 rounded-[20px] bg-[#F8F5FB] px-7 py-6">
          <div className="text-[16px] font-semibold">{summary.title}</div>
          <div className="mt-3 text-[16px] leading-[26px] text-[#49454F]">{summary.text}</div>
        </div>
      )}
      {sections.map((s) => (
        <section key={s.t} className={sectionGap}>
          <h2 className="m-0 text-[24px] font-bold leading-[30px] tracking-[-0.3px]">{s.t}</h2>
          {s.ps.map((p, i) => (
            <p key={i} className="mt-[14px] text-pretty text-[16px] leading-[26px] text-[#49454F]">{p}</p>
          ))}
        </section>
      ))}
    </article>
  )
}

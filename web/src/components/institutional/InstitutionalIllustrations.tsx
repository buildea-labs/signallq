import { IllustrationWrapper } from './InstitutionalFoundation'

type IllustrationProps = { label?: string }

/** SVGs leves, semânticos quando recebem label e decorativos nos demais usos. */
export function ConnectionIllustration({ label }: IllustrationProps) {
  return (
    <IllustrationWrapper alt={label} className="sm:max-w-[360px]">
      <svg viewBox="0 0 360 160" role={label ? 'img' : undefined} aria-label={label} className="h-auto w-full" fill="none">
        <path d="M51 105C86 62 122 62 157 105S228 148 263 105" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeDasharray="6 9" opacity=".55" />
        <circle cx="48" cy="107" r="24" fill="var(--depth-level1-tint)" stroke="currentColor" strokeWidth="2" />
        <path d="M38 110h20M48 100v20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        <circle cx="180" cy="80" r="34" fill="var(--bg-secondary)" stroke="currentColor" strokeWidth="2" />
        <path d="M164 83c9-13 23-13 32 0M170 91c6-7 14-7 20 0M177 99h6" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
        <circle cx="270" cy="107" r="24" fill="var(--depth-level1-tint)" stroke="currentColor" strokeWidth="2" />
        <path d="M258 113h24l-4-19h-16l-4 19Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
      </svg>
    </IllustrationWrapper>
  )
}

export function MeasurementIllustration({ label }: IllustrationProps) {
  return (
    <IllustrationWrapper alt={label} className="sm:max-w-[360px]">
      <svg viewBox="0 0 360 160" role={label ? 'img' : undefined} aria-label={label} className="h-auto w-full" fill="none">
        <path d="M46 116h268" stroke="var(--border)" strokeWidth="2" />
        <path d="M66 108 116 82l47 14 55-52 72 39" stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="66" cy="108" r="6" fill="currentColor" /><circle cx="116" cy="82" r="6" fill="currentColor" /><circle cx="163" cy="96" r="6" fill="currentColor" /><circle cx="218" cy="44" r="6" fill="currentColor" /><circle cx="290" cy="83" r="6" fill="currentColor" />
        <path d="M42 42h48M42 54h28" stroke="var(--text-secondary)" strokeWidth="3" strokeLinecap="round" opacity=".7" />
        <path d="M272 117v-20m12 20V88m12 29v-42" stroke="var(--accent-blue)" strokeWidth="5" strokeLinecap="round" />
      </svg>
    </IllustrationWrapper>
  )
}

export function TermsIllustration({ label }: IllustrationProps) {
  return (
    <IllustrationWrapper alt={label} className="sm:max-w-[300px]">
      <svg viewBox="0 0 300 160" role={label ? 'img' : undefined} aria-label={label} className="h-auto w-full" fill="none">
        <path d="M88 26h95l35 35v72a14 14 0 0 1-14 14H88a14 14 0 0 1-14-14V40a14 14 0 0 1 14-14Z" fill="var(--bg-secondary)" stroke="currentColor" strokeWidth="2" />
        <path d="M183 26v35h35M103 79h85M103 96h85M103 113h50" stroke="currentColor" strokeWidth="3" strokeLinecap="round" opacity=".75" />
        <circle cx="217" cy="118" r="25" fill="var(--depth-level1-tint)" stroke="var(--accent-blue)" strokeWidth="2" />
        <path d="m205 118 8 8 16-19" stroke="var(--accent-blue)" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </IllustrationWrapper>
  )
}

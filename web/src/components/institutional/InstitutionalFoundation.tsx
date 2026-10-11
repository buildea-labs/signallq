import type { ReactNode } from 'react'
import Link from 'next/link'

type IllustrationProps = {
  children: ReactNode
  /** Descreve apenas ilustrações que carregam informação além do texto. */
  alt?: string
  className?: string
}

/**
 * Moldura opcional para SVGs institucionais. O conteúdo textual da página
 * permanece suficiente sem a ilustração; por isso, arte decorativa fica
 * escondida da árvore de acessibilidade por padrão.
 */
export function IllustrationWrapper({ children, alt, className = '' }: IllustrationProps) {
  return (
    <figure
      aria-hidden={alt ? undefined : true}
      className={`m-0 flex w-full max-w-[320px] items-center justify-center text-[var(--accent)] ${className}`}
    >
      {children}
      {alt && <figcaption className="sr-only">{alt}</figcaption>}
    </figure>
  )
}

export function ReadingLayout({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <div className={`w-full max-w-[720px] ${className}`}>{children}</div>
}

type InstitutionalHeroProps = {
  overline?: string
  title: string
  summary?: string
  meta?: ReactNode
  illustration?: ReactNode
}

export function InstitutionalHero({ overline, title, summary, meta, illustration }: InstitutionalHeroProps) {
  return (
    <header className="flex w-full flex-col gap-3 sm:gap-4">
      <div className="flex flex-col gap-2">
        {overline && <p className="label-overline m-0">{overline}</p>}
        <h1 className="headline-large m-0 max-w-[720px] text-balance">{title}</h1>
        {summary && <p className="body-medium m-0 max-w-[720px] text-pretty">{summary}</p>}
        {meta && <div className="body-small" style={{ color: 'var(--text-tertiary)' }}>{meta}</div>}
      </div>
      {illustration && <div className="pt-1">{illustration}</div>}
    </header>
  )
}

export function HighlightSection({ title, children }: { title?: string; children: ReactNode }) {
  return (
    <section
      className="flex flex-col gap-2 rounded-[var(--radius-card)] border-l-4 px-4 py-3"
      style={{ background: 'var(--bg-secondary)', borderColor: 'var(--accent)' }}
    >
      {title && <h2 className="title-medium m-0">{title}</h2>}
      <div className="body-medium [&>p]:m-0">{children}</div>
    </section>
  )
}

export type InstitutionalStep = { title: string; description: ReactNode }

export function StepsBlock({ title, steps }: { title?: string; steps: InstitutionalStep[] }) {
  return (
    <section className="flex flex-col gap-3">
      {title && <h2 className="title-large m-0">{title}</h2>}
      <ol className="m-0 flex list-none flex-col gap-3 p-0">
        {steps.map((step, index) => (
          <li key={step.title} className="grid grid-cols-[32px_minmax(0,1fr)] gap-3">
            <span
              aria-hidden="true"
              className="label-large flex size-8 items-center justify-center rounded-full"
              style={{ background: 'var(--depth-level1-tint)', color: 'var(--accent)' }}
            >
              {index + 1}
            </span>
            <div className="flex flex-col gap-1 pt-1">
              <h3 className="title-medium m-0">{step.title}</h3>
              <div className="body-medium [&>p]:m-0">{step.description}</div>
            </div>
          </li>
        ))}
      </ol>
    </section>
  )
}

export type InformationItem = { label: string; value: ReactNode }

/** Grupo de pares chave-valor: organiza informação sem criar uma grade de cards. */
export function InformationGroup({ title, items }: { title?: string; items: InformationItem[] }) {
  return (
    <section className="flex flex-col gap-2">
      {title && <h2 className="title-large m-0">{title}</h2>}
      <dl className="m-0 divide-y" style={{ borderColor: 'var(--border)' }}>
        {items.map((item) => (
          <div key={item.label} className="grid gap-1 py-3 sm:grid-cols-[minmax(140px,0.35fr)_1fr] sm:gap-4">
            <dt className="label-large">{item.label}</dt>
            <dd className="body-medium m-0">{item.value}</dd>
          </div>
        ))}
      </dl>
    </section>
  )
}

export type AccordionItem = { title: string; content: ReactNode; defaultOpen?: boolean }

/** Usa details/summary nativos para preservar teclado e estado expandido sem JavaScript. */
export function AccessibleAccordion({ title, items }: { title?: string; items: AccordionItem[] }) {
  return (
    <section className="flex flex-col gap-2">
      {title && <h2 className="title-large m-0">{title}</h2>}
      <div className="divide-y rounded-[var(--radius-card)] border" style={{ borderColor: 'var(--border)' }}>
        {items.map((item) => (
          <details key={item.title} open={item.defaultOpen} className="group px-4">
            <summary className="title-medium flex cursor-pointer list-none items-center justify-between gap-4 py-4 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--accent)]">
              {item.title}
              <span aria-hidden="true" className="material-symbols-outlined transition-transform duration-200 group-open:rotate-180">expand_more</span>
            </summary>
            <div className="body-medium pb-4 [&>p]:m-0">{item.content}</div>
          </details>
        ))}
      </div>
    </section>
  )
}

export function InstitutionalCta({ label, href, supportingText }: { label: string; href: string; supportingText?: string }) {
  return (
    <section className="flex flex-col items-start gap-2 pt-1">
      {supportingText && <p className="body-medium m-0">{supportingText}</p>}
      <Link
        href={href}
        className="label-large flex h-10 w-fit items-center justify-center rounded-[var(--radius-button)] px-5 no-underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--accent)]"
        style={{ background: 'var(--accent)', color: 'var(--on-accent)' }}
      >
        {label}
      </Link>
    </section>
  )
}

import Link from 'next/link'
import { Banda } from '@/components/Banda'
import { InstitutionalCta, ReadingLayout } from '@/components/institutional/InstitutionalFoundation'
import { SITE_ORIGIN } from '@/lib/routeMetadata'
import { getRelatedGuides, type Guide } from '../guides-data'

export default function GuideContent({ guide }: { guide: Guide }) {
  const related = getRelatedGuides(guide.slug)
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'Article',
    headline: guide.title,
    description: guide.intro,
    inLanguage: 'pt-BR',
    mainEntityOfPage: `${SITE_ORIGIN}/guias/${guide.slug}`,
    author: { '@type': 'Organization', name: 'SignallQ' },
    publisher: { '@type': 'Organization', name: '7Agents Tecnologia', url: SITE_ORIGIN },
  }

  return (
    <Banda className="py-8 md:py-12 lg:py-16">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <ReadingLayout className="flex flex-col gap-10">
        <article className="flex flex-col gap-6">
          <Link href="/guias" className="label-large no-underline w-fit" style={{ color: 'var(--accent)' }}>
            ← Todos os guias
          </Link>
          <div>
            <h1 className="headline-large m-0 text-balance">{guide.title}</h1>
            <p className="body-medium mt-4 text-pretty" style={{ color: 'var(--text-secondary)' }}>{guide.intro}</p>
          </div>
          {guide.sections.map((section) => (
            <section key={section.h} className="flex flex-col gap-3">
              <h2 className="title-large m-0">{section.h}</h2>
              {section.ps.map((p) => (
                <p key={p} className="body-medium m-0 text-pretty" style={{ color: 'var(--text-secondary)' }}>{p}</p>
              ))}
            </section>
          ))}
        </article>

        <section className="flex flex-wrap items-center justify-between gap-4 rounded-[var(--radius-card)] px-6 py-6" style={{ background: 'var(--bg-secondary)' }}>
          <div className="max-w-[22em]">
            <div className="title-medium m-0">{guide.ctaTitle}</div>
            <div className="body-small mt-1" style={{ color: 'var(--text-secondary)' }}>{guide.ctaText}</div>
          </div>
          <InstitutionalCta label={guide.ctaLabel} href={guide.ctaHref} />
        </section>

        {related.length > 0 && (
          <section className="flex flex-col gap-2 border-t pt-6" style={{ borderColor: 'var(--border)' }}>
            <div className="label-large" style={{ color: 'var(--text-secondary)' }}>Leia também</div>
            {related.map((r) => (
              <Link key={r.slug} href={`/guias/${r.slug}`} className="title-medium block py-2 no-underline" style={{ color: 'var(--text-primary)' }}>
                {r.title}
              </Link>
            ))}
          </section>
        )}
      </ReadingLayout>
    </Banda>
  )
}

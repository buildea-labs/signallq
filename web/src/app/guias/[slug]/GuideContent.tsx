import Link from 'next/link'
import { SITE_ORIGIN } from '@/lib/routeMetadata'
import { getRelatedGuides, type Guide } from '../guides-data'

export default function GuideContent({ guide }: { guide: Guide }) {
  const related = getRelatedGuides(guide.slug)
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'Article',
    headline: guide.title,
    description: guide.summary,
    inLanguage: 'pt-BR',
    mainEntityOfPage: `${SITE_ORIGIN}/guias/${guide.slug}`,
    datePublished: '2026-10-10',
    dateModified: '2026-10-10',
    author: { '@type': 'Organization', name: 'SignallQ' },
    publisher: { '@type': 'Organization', name: '7Agents Tecnologia', url: SITE_ORIGIN },
  }

  return (
    <article className="mx-auto max-w-[768px] bg-white px-6 pb-24 pt-20 font-sans leading-[normal] text-[#1C1B1F]">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <Link href="/guias" className="text-[14px] font-medium text-[#5B21D6] no-underline">← Todos os guias</Link>
      <h1 className="m-0 mt-6 text-balance text-[clamp(34px,4.6vw,50px)] font-bold leading-[1.06] tracking-[-1.2px]">{guide.title}</h1>
      <p className="mt-6 text-pretty text-[20px] leading-[30px] text-[#49454F]">{guide.intro}</p>
      {guide.sections.map((section) => (
        <section key={section.h} className="mt-12">
          <h2 className="m-0 text-[26px] font-bold leading-8 tracking-[-0.4px]">{section.h}</h2>
          {section.ps.map((p) => (
            <p key={p} className="mt-[14px] text-pretty text-[17px] leading-[27px] text-[#49454F]">{p}</p>
          ))}
        </section>
      ))}
      <div className="mt-16 flex flex-wrap items-center justify-between gap-x-6 gap-y-5 rounded-3xl bg-[#EAE0FF] p-8">
        <div className="max-w-[22em]">
          <div className="text-[22px] font-semibold leading-7 text-[#210A5C]">{guide.ctaTitle}</div>
          <div className="mt-[6px] text-[15px] leading-[22px] text-[#210A5C]">{guide.ctaText}</div>
        </div>
        <Link href={guide.ctaHref} className="rounded-full bg-[#5B21D6] px-[26px] py-[14px] text-[15px] font-medium text-white no-underline">
          {guide.ctaLabel}
        </Link>
      </div>
      <div className="mt-12 border-t border-[#E7E0EC] pt-6">
        <div className="mb-2 text-[14px] font-semibold text-[#49454F]">Leia também</div>
        {related.map((r) => (
          <Link key={r.slug} href={`/guias/${r.slug}`} className="block py-[10px] text-[17px] font-medium text-[#5B21D6] no-underline">
            {r.title}
          </Link>
        ))}
      </div>
    </article>
  )
}

import Link from 'next/link'
import { Banda } from '@/components/Banda'
import { InstitutionalHero, ReadingLayout } from '@/components/institutional/InstitutionalFoundation'
import { GUIDES } from './guides-data'

export default function GuiasIndexContent() {
  return (
    <Banda className="py-8 md:py-12 lg:py-16">
      <ReadingLayout className="flex flex-col gap-8">
        <InstitutionalHero overline="Guias" title="Entenda sua internet, sem jargão." />
        <div className="flex flex-col">
          {GUIDES.map((guide) => (
            <Link
              key={guide.slug}
              href={`/guias/${guide.slug}`}
              className="block border-t py-6 no-underline"
              style={{ borderColor: 'var(--border)', color: 'var(--text-primary)' }}
            >
              <div className="title-large m-0">{guide.title}</div>
              <div className="body-medium mt-2 max-w-[36em]" style={{ color: 'var(--text-secondary)' }}>{guide.intro}</div>
            </Link>
          ))}
        </div>
      </ReadingLayout>
    </Banda>
  )
}

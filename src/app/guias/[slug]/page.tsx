import type { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { PAGE_META } from '@/lib/pageMetaCatalog'
import { routeMetadata } from '@/lib/routeMetadata'
import { GUIDES, getGuide } from '../guides-data'
import GuideContent from './GuideContent'

export function generateStaticParams() {
  return GUIDES.map((guide) => ({ slug: guide.slug }))
}

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params
  const meta = PAGE_META[`/guias/${slug}`]
  return meta ? routeMetadata(meta) : {}
}

export default async function GuiaPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params
  const guide = getGuide(slug)
  if (!guide) notFound()
  return <GuideContent guide={guide} />
}

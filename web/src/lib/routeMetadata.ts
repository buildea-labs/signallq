import type { Metadata } from 'next'
import type { PageMeta } from './seo'

/** Canonical public origin shared by route metadata and user-facing links. */
export const SITE_ORIGIN = 'https://signallq.com'

/** Metadados renderizados no HTML inicial das rotas públicas. */
export function routeMetadata(meta: PageMeta): Metadata {
  const canonical = `${SITE_ORIGIN}${meta.path}`
  const image = `${SITE_ORIGIN}${meta.ogImage ?? '/signallq-symbol.png'}`
  const index = meta.robots !== 'noindex,follow'

  return {
    title: meta.title,
    description: meta.description,
    alternates: { canonical },
    robots: { index, follow: true },
    openGraph: {
      type: 'website',
      url: canonical,
      title: meta.title,
      description: meta.description,
      images: [{ url: image }],
    },
    twitter: { card: 'summary_large_image', title: meta.title, description: meta.description, images: [image] },
  }
}

import { describe, expect, it } from 'vitest'
import { routeMetadata, SITE_ORIGIN } from './routeMetadata'

describe('routeMetadata', () => {
  it('uses the official public origin in canonical and Open Graph URLs', () => {
    const metadata = routeMetadata({
      title: 'Página de teste',
      description: 'Descrição de teste.',
      path: '/como-medimos',
    })

    expect(SITE_ORIGIN).toBe('https://signallq.com')
    expect(metadata.alternates).toEqual({ canonical: 'https://signallq.com/como-medimos' })
    expect(metadata.openGraph).toMatchObject({ url: 'https://signallq.com/como-medimos' })
  })
})

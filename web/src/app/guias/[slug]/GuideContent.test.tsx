import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import GuideContent from './GuideContent'
import { getGuide } from '../guides-data'

describe('GuideContent', () => {
  it('renders the guide body, CTA and related guides, with Article JSON-LD', () => {
    const guide = getGuide('por-que-minha-internet-esta-lenta')!
    const markup = renderToStaticMarkup(<GuideContent guide={guide} />)
    expect(markup).toContain(guide.title)
    expect(markup).toContain('Comece separando Wi-Fi de internet')
    expect(markup).toContain('Leia também')
    expect(markup).toContain('"@type":"Article"')
    // não deve listar a si mesmo em "Leia também"
    expect(markup.match(/guias\/por-que-minha-internet-esta-lenta/g)?.length).toBe(1)
  })
})

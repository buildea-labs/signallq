import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import GuiasPage from './page'
import { GUIDES } from './guides-data'

describe('guias index', () => {
  it('lists all four guides with their intro', () => {
    const markup = renderToStaticMarkup(<GuiasPage />)
    for (const guide of GUIDES) {
      expect(markup).toContain(guide.title)
      expect(markup).toContain(`/guias/${guide.slug}`)
    }
  })
})

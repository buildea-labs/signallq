import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import DuvidasPage from './page'

describe('dúvidas', () => {
  it('renders the three question groups and the FAQPage JSON-LD', () => {
    const markup = renderToStaticMarkup(<DuvidasPage />)
    expect(markup).toContain('Sobre o app')
    expect(markup).toContain('Diagnóstico')
    expect(markup).toContain('Privacidade')
    expect(markup).toContain('suporte@signallq.com')
    expect(markup).toContain('"@type":"FAQPage"')
  })
})

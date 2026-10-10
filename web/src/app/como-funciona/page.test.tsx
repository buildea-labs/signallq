import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import ComoFuncionaPage from './page'

describe('como funciona', () => {
  it('renders the four-step explainer, the analyzable tools and the closing CTA', () => {
    const markup = renderToStaticMarkup(<ComoFuncionaPage />)
    expect(markup).toContain('Do sintoma à solução, em quatro passos.')
    expect(markup).toContain('Entender')
    expect(markup).toContain('Confirmar')
    expect(markup).toContain('Mapa do Wi-Fi Casa')
    expect(markup).toContain('Pronto para descobrir o que está acontecendo?')
    expect(markup).toContain('Baixar no Google Play')
  })
})

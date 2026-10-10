import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import ComoFuncionaPage from './page'

describe('como funciona', () => {
  it('renders the four-step explainer and the analyzable tools', () => {
    const markup = renderToStaticMarkup(<ComoFuncionaPage />)
    expect(markup).toContain('Do sintoma à solução, em quatro passos.')
    expect(markup).toContain('Entender')
    expect(markup).toContain('Confirmar')
    expect(markup).toContain('Mapa do Wi-Fi Casa')
    expect(markup).toContain('Fazer o teste de velocidade')
  })
})

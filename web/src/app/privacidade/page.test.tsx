import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import PrivacyPage from './page'

describe('privacy policy', () => {
  it('renders the Android app policy with essential rights and contact', () => {
    const markup = renderToStaticMarkup(<PrivacyPage />)
    expect(markup).toContain('No aplicativo Android')
    expect(markup).toContain('Seus direitos e como falar conosco')
    expect(markup).toContain('suporte@signallq.com')
  })
})

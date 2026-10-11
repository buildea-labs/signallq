import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import {
  AccessibleAccordion,
  IllustrationWrapper,
  InformationGroup,
  StepsBlock,
} from './InstitutionalFoundation'

describe('institutional foundation', () => {
  it('keeps information and steps in semantic list structures', () => {
    const markup = renderToStaticMarkup(
      <>
        <InformationGroup items={[{ label: 'Retenção', value: 'Local' }]} />
        <StepsBlock steps={[{ title: 'Medir', description: 'Execute o teste.' }]} />
      </>,
    )

    expect(markup).toContain('<dl')
    expect(markup).toContain('<dt')
    expect(markup).toContain('<ol')
  })

  it('uses native disclosure and treats unlabelled illustrations as decorative', () => {
    const markup = renderToStaticMarkup(
      <>
        <AccessibleAccordion items={[{ title: 'Detalhes', content: 'Conteúdo', defaultOpen: true }]} />
        <IllustrationWrapper><svg /></IllustrationWrapper>
      </>,
    )

    expect(markup).toContain('<details open=""')
    expect(markup).toContain('<summary')
    expect(markup).toContain('aria-hidden="true"')
  })
})

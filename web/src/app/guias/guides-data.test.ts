import { describe, expect, it } from 'vitest'
import { GUIDES, getGuide, getRelatedGuides } from './guides-data'

describe('guides-data', () => {
  it('has four guides, each with sections and a CTA', () => {
    expect(GUIDES).toHaveLength(4)
    for (const guide of GUIDES) {
      expect(guide.sections.length).toBeGreaterThan(0)
      expect(guide.ctaHref).toBeTruthy()
    }
  })

  it('getGuide finds a guide by slug and returns undefined otherwise', () => {
    expect(getGuide('por-que-minha-internet-esta-lenta')?.title).toBe('Por que minha internet está lenta?')
    expect(getGuide('nao-existe')).toBeUndefined()
  })

  it('getRelatedGuides excludes the current slug and caps at the limit', () => {
    const related = getRelatedGuides('por-que-minha-internet-esta-lenta')
    expect(related).toHaveLength(3)
    expect(related.every((g) => g.slug !== 'por-que-minha-internet-esta-lenta')).toBe(true)
  })
})

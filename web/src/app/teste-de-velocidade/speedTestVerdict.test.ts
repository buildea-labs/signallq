import { describe, expect, it } from 'vitest'
import { formatMetric, getUsageLevels, getVerdict } from './speedTestVerdict'

describe('formatMetric', () => {
  it('formats sub-100 values with one decimal and 100+ as integers', () => {
    expect(formatMetric(8.456)).toBe('8.5')
    expect(formatMetric(142.9)).toBe('143')
    expect(formatMetric(null)).toBe('—')
  })
})

describe('getVerdict', () => {
  it('classifies download speed into the four bands', () => {
    expect(getVerdict(150).title).toBe('Sua internet está ótima.')
    expect(getVerdict(40).title).toBe('Sua internet está boa.')
    expect(getVerdict(10).title).toBe('Sua internet dá para o básico.')
    expect(getVerdict(2).title).toBe('Internet lenta.')
    expect(getVerdict(null).title).toBe('Internet lenta.')
  })
})

describe('getUsageLevels', () => {
  it('rates a fast, low-latency connection as tranquilo across the board', () => {
    const levels = getUsageLevels(150, 20, 15)
    expect(levels.every((l) => l.s === 'Tranquilo')).toBe(true)
  })

  it('rates a slow connection as travando', () => {
    const levels = getUsageLevels(1, 0.5, 300)
    const hd = levels.find((l) => l.n === 'Vídeo em HD')
    expect(hd?.s).toBe('Vai travar')
  })

  it('handles null measurements as zero without throwing', () => {
    expect(() => getUsageLevels(null, null, null)).not.toThrow()
  })
})

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { SiteNav } from './SiteNav'

const { usePathnameMock } = vi.hoisted(() => ({ usePathnameMock: vi.fn(() => '/') }))
vi.mock('next/navigation', () => ({ usePathname: usePathnameMock }))

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})

describe('SiteNav — landing pública do app Android', () => {
  it('linka o logo para a home e mostra o botão de download da Play Store', () => {
    render(<SiteNav />)
    expect(screen.getByRole('link', { name: 'Página inicial SignallQ' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('button', { name: /Baixar/i })).toBeInTheDocument()
  })

  it('renders the four site nav links', () => {
    render(<SiteNav />)
    for (const label of ['Teste de velocidade', 'Guias', 'Como funciona', 'Dúvidas']) {
      expect(screen.getByRole('link', { name: label })).toBeInTheDocument()
    }
  })

  it('marks the link matching the current route as active (aria-current)', () => {
    usePathnameMock.mockReturnValue('/guias/por-que-minha-internet-esta-lenta')
    render(<SiteNav />)
    expect(screen.getByRole('link', { name: 'Guias' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('link', { name: 'Dúvidas' })).not.toHaveAttribute('aria-current')
  })
})

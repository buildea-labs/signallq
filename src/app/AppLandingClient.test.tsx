import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AppLandingClient } from './AppLandingClient'
import * as telemetry from '@/lib/telemetry'

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})

describe('página / — AppLandingClient (landing pública do app Android)', () => {
  beforeEach(() => {
    vi.spyOn(window, 'open').mockImplementation(() => null)
  })

  // Alinhado ao GALLERY_ITEMS real de AppLandingComponents.tsx (reduzido a
  // 4 capturas em afd3d3a, "redesign web to match DS 2.0") — este teste
  // ficou descrevendo um set de 8 telas (fluxo de teste/diagnóstico) que já
  // não existe desde aquele commit; corrigido pra refletir a galeria atual.
  it('renderiza a galeria com as 4 capturas reais, cada uma com alt text específico', () => {
    render(<AppLandingClient />)

    const expectedAlts = [
      /Tela Início do SignallQ\./,
      /Tela de Velocidade do SignallQ\./,
      /Tela de Histórico\./,
      /Tela de Ferramentas\./,
    ]

    for (const pattern of expectedAlts) {
      expect(screen.getByAltText(pattern)).toBeInTheDocument()
    }

    // 2 ocorrências esperadas: a captura real do hero ("Conexão excelente")
    // e a tela escura "Início" na galeria — composições diferentes da mesma tela.
    expect(screen.queryAllByAltText(/^Tela Início do SignallQ/)).toHaveLength(2)
  })

  it('a galeria é uma grade de <figure>/<figcaption> navegável', () => {
    render(<AppLandingClient />)
    const figures = document.querySelectorAll('figure')
    expect(figures.length).toBe(4)
    figures.forEach((fig) => {
      expect(fig.querySelector('img')).not.toBeNull()
      expect(fig.querySelector('figcaption')).not.toBeNull()
    })
  })

  it('exibe o bloco de requisitos/privacidade com link para /privacidade', () => {
    render(<AppLandingClient />)
    expect(screen.getByRole('link', { name: 'Privacidade' })).toHaveAttribute('href', '/privacidade')
  })

  it('mantém o CTA de download na faixa final, com copy distinta do hero', () => {
    render(<AppLandingClient />)
    const primaryButtons = screen.getAllByRole('button', { name: 'Baixar na Play Store' })
    expect(primaryButtons).toHaveLength(1)
    expect(screen.getByRole('button', { name: 'Baixar grátis na Play Store' })).toBeInTheDocument()
  })

  it('dispara telemetria de download ao clicar no CTA e abre a Play Store', async () => {
    const trackSpy = vi.spyOn(telemetry, 'trackFeatureUsed')
    const { default: userEvent } = await import('@testing-library/user-event')
    const user = userEvent.setup()
    render(<AppLandingClient />)

    const [primaryHero] = screen.getAllByRole('button', { name: 'Baixar na Play Store' })
    await user.click(primaryHero)
    expect(trackSpy).toHaveBeenCalledWith('download_app_clicado')
    expect(window.open).toHaveBeenCalledWith(
      expect.stringContaining('play.google.com/store/apps/details?id=io.signallq.app'),
      '_blank',
      'noopener,noreferrer',
    )
  })
})

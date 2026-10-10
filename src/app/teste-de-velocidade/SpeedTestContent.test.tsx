import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import * as telemetry from '@/lib/telemetry'
import { SpeedTestContent } from './SpeedTestContent'

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
})

describe('/teste-de-velocidade — SpeedTestContent', () => {
  it('mostra a tela inicial com o botão "Iniciar teste"', () => {
    render(<SpeedTestContent />)
    expect(screen.getByRole('button', { name: 'Iniciar teste' })).toBeInTheDocument()
  })

  it('dispara telemetria e transiciona para o estado "running" ao iniciar', async () => {
    const trackSpy = vi.spyOn(telemetry, 'trackFeatureUsed')
    // nunca resolve — só precisamos observar a transição pra "running"
    vi.stubGlobal('fetch', vi.fn(() => new Promise(() => {})))
    const { default: userEvent } = await import('@testing-library/user-event')
    const user = userEvent.setup()

    render(<SpeedTestContent />)
    await user.click(screen.getByRole('button', { name: 'Iniciar teste' }))

    expect(trackSpy).toHaveBeenCalledWith('teste_velocidade_iniciado')
    await waitFor(() => expect(screen.getByText('Medindo latência…')).toBeInTheDocument())
  })

  it('vai para o estado de erro quando a medição falha', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new Error('offline'))))
    const { default: userEvent } = await import('@testing-library/user-event')
    const user = userEvent.setup()

    render(<SpeedTestContent />)
    await user.click(screen.getByRole('button', { name: 'Iniciar teste' }))

    await waitFor(() => expect(screen.getByText('Não conseguimos medir.')).toBeInTheDocument())
  })
})

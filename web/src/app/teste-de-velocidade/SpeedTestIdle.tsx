export function SpeedTestIdle({ onStart }: { onStart: () => void }) {
  return (
    <div className="text-center">
      <div className="label-overline" style={{ color: 'var(--accent)' }}>Teste de velocidade</div>
      <h1 className="headline-large m-0 mt-5 text-balance">Quão rápida está sua internet agora?</h1>
      <p className="body-medium mx-auto mt-5 max-w-[28em] text-pretty" style={{ color: 'var(--text-secondary)' }}>
        Leva cerca de 20 segundos. Você vê o resultado em palavras, não só em números.
      </p>
      <button
        type="button"
        onClick={onStart}
        className="label-large mt-10 rounded-[var(--radius-button)] px-10 py-5 text-[18px]"
        style={{ background: 'var(--accent)', color: 'var(--on-accent)' }}
      >
        Iniciar teste
      </button>
      <div className="body-small mt-4" style={{ color: 'var(--text-secondary)' }}>
        Mede latência, download e upload. Nenhum dado pessoal é coletado.
      </div>
    </div>
  )
}

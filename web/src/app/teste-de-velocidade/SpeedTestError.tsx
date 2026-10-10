import Link from 'next/link'

export function SpeedTestError({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="text-center">
      <h1 className="headline-large m-0 text-balance">Não conseguimos medir.</h1>
      <p className="body-medium mx-auto mt-4 max-w-[26em] text-pretty" style={{ color: 'var(--text-secondary)' }}>
        Verifique se você está conectado e tente de novo. Se continuar, o app consegue investigar a causa.
      </p>
      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <button
          type="button"
          onClick={onRetry}
          className="label-large rounded-[var(--radius-button)] px-7 py-4"
          style={{ background: 'var(--accent)', color: 'var(--on-accent)' }}
        >
          Tentar de novo
        </button>
        <Link
          href="/#baixar"
          className="label-large flex items-center justify-center rounded-[var(--radius-button)] px-7 py-4 no-underline"
          style={{ background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}
        >
          Baixar o app
        </Link>
      </div>
    </div>
  )
}

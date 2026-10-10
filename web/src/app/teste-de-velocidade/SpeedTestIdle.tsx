export function SpeedTestIdle({ onStart }: { onStart: () => void }) {
  return (
    <div className="text-center">
      <div className="mb-5 text-[14px] font-medium text-[#5B21D6]">Teste de velocidade</div>
      <h1 className="m-0 text-balance text-[clamp(36px,5vw,56px)] font-bold leading-[1.05] tracking-[-1.4px]">
        Quão rápida está sua internet agora?
      </h1>
      <p className="mx-auto mt-5 max-w-[28em] text-[18px] leading-7 text-[#49454F]">
        Leva cerca de 20 segundos. Você vê o resultado em palavras, não só em números.
      </p>
      <button
        type="button"
        onClick={onStart}
        className="mt-10 cursor-pointer rounded-full border-0 bg-[#5B21D6] px-12 py-5 text-[18px] font-medium text-white"
      >
        Iniciar teste
      </button>
      <div className="mt-4 text-[14px] text-[#49454F]">Mede latência, download e upload. Nenhum dado pessoal é coletado.</div>
    </div>
  )
}

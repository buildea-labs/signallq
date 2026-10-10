import Link from 'next/link'

export function SpeedTestError({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="text-center">
      <h1 className="m-0 text-[clamp(32px,4.4vw,44px)] font-bold leading-[1.1] tracking-[-1px]">Não conseguimos medir.</h1>
      <p className="mx-auto mt-4 max-w-[26em] text-[18px] leading-7 text-[#49454F]">
        Verifique se você está conectado e tente de novo. Se continuar, o app consegue investigar a causa.
      </p>
      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <button type="button" onClick={onRetry} className="cursor-pointer rounded-full border-0 bg-[#5B21D6] px-[30px] py-[15px] text-[16px] font-medium text-white">
          Tentar de novo
        </button>
        <Link href="/#baixar" className="rounded-full bg-[#F3EEFA] px-[30px] py-[15px] text-[16px] font-medium text-[#210A5C] no-underline">
          Baixar o app
        </Link>
      </div>
    </div>
  )
}

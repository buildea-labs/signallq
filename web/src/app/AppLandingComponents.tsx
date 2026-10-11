"use client";
import Image from 'next/image'
import Link from 'next/link'

const PRIMARY = 'rounded-full bg-[#5B21D6] font-medium text-white no-underline'

export function AppLandingHero({ onBaixar }: { onBaixar: () => void }) {
  return (
    <section
      id="topo"
      className="mx-auto grid w-full max-w-[1168px] grid-cols-[repeat(auto-fit,minmax(320px,1fr))] items-center gap-16 px-6 pb-[88px] pt-24"
    >
      <div>
        <div className="mb-5 text-[14px] font-medium text-[#5B21D6]">Diagnóstico de internet para Android</div>
        <h1 className="m-0 text-balance text-[clamp(40px,5.6vw,64px)] font-bold leading-[1.04] tracking-[-1.5px]">
          Descubra por que sua internet está ruim.
        </h1>
        <p className="mt-6 max-w-[30em] text-pretty text-[20px] leading-[30px] text-[#49454F]">
          O SignallQ analisa sua rede, mostra onde está o problema e diz o que fazer. Depois, confirma se resolveu.
        </p>
        <div className="mt-9 flex flex-wrap gap-3">
          <button type="button" onClick={onBaixar} className={`${PRIMARY} cursor-pointer border-0 px-7 py-4 text-[16px]`}>
            Baixar no Google Play
          </button>
          <Link href="/teste-de-velocidade" className="rounded-full bg-[#F3EEFA] px-7 py-4 text-[16px] font-medium text-[#210A5C] no-underline">
            Fazer teste de velocidade
          </Link>
        </div>
        <div className="mt-5 text-[14px] text-[#49454F]">Grátis · Sem cadastro para começar</div>
      </div>
      <div className="flex justify-center">
        <div className="w-[300px] rounded-[40px] bg-black p-[10px] shadow-[0_30px_60px_-20px_rgba(33,10,92,0.35)]">
          <Image
            src="/assets/playstore/09-inicio-conexao-excelente.jpg"
            alt="Tela Início do SignallQ mostrando o diagnóstico Conexão excelente"
            width={280}
            height={572}
            priority
            className="block h-[572px] w-full rounded-[32px] object-cover object-bottom"
          />
        </div>
      </div>
    </section>
  )
}

export function AppLandingCTA({ onBaixar }: { onBaixar: () => void }) {
  return (
    <section id="baixar" className="scroll-mt-20 px-6 pb-28 pt-6">
      <div className="mx-auto max-w-[1120px] rounded-[28px] bg-[#EAE0FF] px-8 py-[72px] text-center">
        <h2 className="mx-auto max-w-[14em] text-balance text-[clamp(30px,4vw,46px)] font-bold leading-[1.08] tracking-[-1px] text-[#210A5C]">
          Sua internet está ruim agora? Descubra em minutos.
        </h2>
        <button type="button" onClick={onBaixar} className={`${PRIMARY} mt-8 inline-block cursor-pointer border-0 px-8 py-4 text-[16px]`}>
          Baixar no Google Play
        </button>
      </div>
    </section>
  )
}

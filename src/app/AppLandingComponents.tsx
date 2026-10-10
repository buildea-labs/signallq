"use client";
import Image from 'next/image'
import Link from 'next/link'

export const APP_DIFERENCIAIS_ID = 'app-diferenciais'

export const FEATURES = [
  { icon: 'wifi', title: 'Wi-Fi cômodo a cômodo', text: 'Descubra o alcance exato do seu roteador e encontre os pontos cegos da casa.' },
  { icon: 'signal_cellular_alt', title: 'Sinal 4G/5G direto da fonte', text: 'Medição precisa do rádio da operadora, mostrando a qualidade real e estabilidade da rede.' },
  { icon: 'psychology', title: 'Causa provável em português', text: 'Receba diagnósticos mastigados dizendo de quem é a culpa (provedor, roteador ou distância).' },
  { icon: 'sports_esports', title: 'Modo Gamer', text: 'Avaliação específica de ping e variação para o seu jogo e console favoritos.' },
];

export const GALLERY_ITEMS = [
  {
    src: '/assets/playstore/01-inicio-escuro.png',
    width: 1080,
    height: 2400,
    alt: "Tela Início do SignallQ.",
    caption: 'Visão geral da sua conexão',
  },
  {
    src: '/assets/playstore/04-velocidade-claro.png',
    width: 1080,
    height: 2400,
    alt: "Tela de Velocidade do SignallQ.",
    caption: 'Teste de velocidade preciso',
  },
  {
    src: '/assets/playstore/05-historico-escuro.png',
    width: 1080,
    height: 2400,
    alt: "Tela de Histórico.",
    caption: 'Histórico detalhado',
  },
  {
    src: '/assets/playstore/08-ferramentas-claro.png',
    width: 1080,
    height: 2400,
    alt: "Tela de Ferramentas.",
    caption: 'Ferramentas avançadas',
  },
];

export function AppLandingHero({
  onBaixar,
  onVerDiferenciais,
}: {
  onBaixar: () => void
  onVerDiferenciais: () => void
}) {
  return (
    <div className="relative w-full box-border flex justify-center overflow-hidden p-[28px_20px_24px] sm:p-[56px_var(--safe-x)_40px]">
      
      <div className="relative w-full max-w-[1080px] flex items-center gap-[48px] flex-wrap">
        <div className="flex-[1_1_420px] flex flex-col items-center sm:items-start gap-4 text-center sm:text-left">
          <h1 className="m-0 max-w-[560px] font-bold text-[34px] leading-[40px] text-[color:var(--text-primary)] text-pretty font-sans">
            Vá além do teste de velocidade. Descubra por que sua internet trava.
          </h1>
          <p className="m-0 max-w-[480px] font-normal text-[16px] leading-[24px] text-[color:var(--text-secondary)] text-pretty font-sans">
            O SignallQ analisa seu Wi-Fi, sinal 4G/5G e ping em tempo real para entregar diagnósticos claros. Saiba exatamente onde está o problema da sua conexão.
          </p>
          <div className="flex flex-wrap justify-center sm:justify-start gap-3 pt-[6px]">
            <button
              type="button"
              onClick={onBaixar}
              className="h-[48px] flex items-center gap-2 px-6 rounded-[20px] border-none bg-[color:var(--accent)] cursor-pointer hover:brightness-110 transition-all shadow-md"
            >
              <span aria-hidden="true" className="material-symbols-outlined text-[20px] text-[color:var(--on-accent)]">download</span>
              <span className="font-semibold text-[15px] leading-[1.2] text-[color:var(--on-accent)] font-sans">Baixar grátis na Play Store</span>
            </button>
          </div>
          <div className="flex items-center gap-2">
            <span className="sq-app-dot w-2 h-2 rounded-[999px] bg-[color:var(--success)]" />
            <span className="font-medium text-[11px] leading-[1.45] tracking-[0.4px] uppercase text-[color:var(--text-tertiary)] font-sans">
              Disponível para Android
            </span>
          </div>
        </div>

        <div className="sq-app-float flex-none rounded-[28px] p-[6px] box-border bg-[#16181d] shadow-md mx-auto sm:mx-0">
          <Image
            src="/assets/playstore/09-inicio-conexao-excelente.jpg"
            alt="Tela Início do SignallQ mostrando o diagnóstico Conexão excelente"
            width={369}
            height={800}
            className="block rounded-[22px]"
          />
        </div>
      </div>

      <button
        type="button"
        onClick={onVerDiferenciais}
        aria-label="Ver diferenciais do app"
        className="sq-app-bounce absolute left-1/2 bottom-2 -translate-x-1/2 hidden sm:flex items-center justify-center rounded-[999px] border-none bg-transparent p-1 cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[color:var(--accent)]"
      >
        <span aria-hidden="true" className="material-symbols-outlined text-[24px] text-[color:var(--text-tertiary)]">expand_more</span>
      </button>
    </div>
  )
}

export function AppLandingFeatures() {
  return (
    <div id={APP_DIFERENCIAIS_ID} tabIndex={-1} className="sq-app-reveal flex flex-col gap-4 scroll-mt-6 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[color:var(--accent)]">
      <div className="text-center font-medium text-[11px] leading-[1.45] text-[color:var(--accent)] tracking-[0.3px] uppercase font-sans">
        Diferenciais
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full max-w-[1080px] mx-auto">
        {FEATURES.map((f, i) => (
          <div key={i} className="flex flex-col gap-2 rounded-[16px] p-[20px] box-border bg-[color:var(--bg-card)]">
            <span aria-hidden="true" className="material-symbols-outlined text-[22px] text-[color:var(--accent)]">{f.icon}</span>
            <div className="font-semibold text-[16px] leading-[22px] text-[color:var(--text-primary)] font-sans">{f.title}</div>
            <div className="font-normal text-[14px] leading-[20px] text-[color:var(--text-secondary)] font-sans">{f.text}</div>
          </div>
        ))}
      </div>
    </div>
  )
}

export function AppLandingGallery() {
  return (
    <div className="sq-app-reveal flex flex-col gap-5">
      <div className="flex flex-col items-center gap-2 text-center">
        <h2 className="m-0 font-bold text-[26px] leading-[32px] text-[color:var(--text-primary)] font-sans">
          Design limpo. Dados precisos.
        </h2>
        <p className="m-0 max-w-[560px] font-normal text-[14px] leading-[20px] text-[color:var(--text-tertiary)] font-sans">
          Veja o aplicativo em ação com capturas reais da interface.
        </p>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 w-full max-w-[1080px] mx-auto">
        {GALLERY_ITEMS.map((item) => (
          <figure
            key={item.src}
            className="m-0 flex flex-col gap-2 rounded-[16px] p-0 box-border bg-transparent"
          >
            <Image
              src={item.src}
              alt={item.alt}
              width={item.width}
              height={item.height}
              sizes="(min-width: 1024px) 25vw, (min-width: 640px) 50vw, 100vw"
              className="w-full h-auto rounded-[14px]"
            />
            <figcaption className="text-center font-medium text-[14px] leading-[20px] text-[color:var(--text-secondary)] font-sans mt-2">
              {item.caption}
            </figcaption>
          </figure>
        ))}
      </div>
    </div>
  )
}

export function AppLandingCTA({ onBaixar }: { onBaixar: () => void }) {
  return (
    <div id="baixar" className="sq-app-reveal flex flex-col gap-5 text-center mt-[32px] scroll-mt-24">
      <div className="flex flex-col items-center gap-4 rounded-[16px] p-[32px] box-border bg-[color:var(--bg-secondary)] mx-auto w-full max-w-[720px]">
        <div className="font-semibold text-[22px] leading-[28px] text-[color:var(--text-primary)] font-sans">
          Pare de adivinhar o problema da sua conexão.
        </div>
        <button
          type="button"
          onClick={onBaixar}
          className="h-[48px] flex items-center gap-2 px-6 rounded-[20px] border-none bg-[color:var(--accent)] cursor-pointer hover:brightness-110 transition-all shadow-sm"
        >
          <span aria-hidden="true" className="material-symbols-outlined text-[20px] text-[color:var(--on-accent)]">download</span>
          <span className="font-semibold text-[15px] leading-[1.2] text-[color:var(--on-accent)] font-sans whitespace-nowrap">Baixar na Play Store</span>
        </button>
      </div>
      
      <p className="m-0 mx-auto font-normal text-[12px] leading-[16px] text-[color:var(--text-tertiary)] font-sans text-pretty mt-2 max-w-[640px]">
        Requer aparelho Android com acesso à Play Store. Veja como tratamos os dados coletados em{' '}
        <Link href="/privacidade" className="text-[color:var(--accent)] hover:underline">Privacidade</Link>.
      </p>
    </div>
  )
}

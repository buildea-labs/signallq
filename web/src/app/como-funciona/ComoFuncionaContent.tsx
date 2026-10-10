import Link from 'next/link'

const STEPS = [
  { n: '01', t: 'Entender', d: 'Você escolhe o que está sentindo: lentidão, travadas, quedas ou jogo com atraso. O sintoma define quais análises vão rodar.', ex: 'Você diz', exv: '“A internet fica lenta à noite.”' },
  { n: '02', t: 'Diagnosticar', d: 'O app mede só o necessário: Wi-Fi, velocidade, DNS, rede móvel. Sem menus técnicos para explorar.', ex: 'Em andamento', exv: 'Verificando sinal, velocidade e DNS…' },
  { n: '03', t: 'Resolver', d: 'Você recebe a causa provável com nível de confiança, as evidências essenciais e uma ação recomendada.', ex: 'Ação recomendada', exv: 'Troque o roteador de lugar, longe de paredes e do micro-ondas.' },
  { n: '04', t: 'Confirmar', d: 'Depois da ação, o app repete a verificação relevante e mostra se houve melhora.', ex: 'Nova verificação', exv: 'Sinal no cômodo: de Fraco para Bom.' },
]

const TOOLS = [
  { t: 'Velocidade', d: 'Download, upload e latência.' },
  { t: 'Wi-Fi', d: 'Sinal, banda e canal.' },
  { t: 'Rede móvel', d: '4G/5G e qualidade do sinal.' },
  { t: 'DNS', d: 'Se os endereços estão respondendo.' },
  { t: 'Dispositivos', d: 'Quem está na sua rede local.' },
  { t: 'Mapa do Wi-Fi Casa', d: 'Sinal em cada cômodo.' },
]

export default function ComoFuncionaContent() {
  return (
    <div className="bg-white font-sans leading-[normal] text-[#1C1B1F]">
      <section className="mx-auto max-w-[1168px] px-6 pb-14 pt-[88px]">
        <div className="mb-5 text-[14px] font-medium text-[#5B21D6]">Como funciona</div>
        <h1 className="m-0 max-w-[14em] text-balance text-[clamp(38px,5vw,58px)] font-bold leading-[1.05] tracking-[-1.4px]">
          Do sintoma à solução, em quatro passos.
        </h1>
        <p className="mt-6 max-w-[30em] text-pretty text-[20px] leading-[30px] text-[#49454F]">
          O SignallQ não começa por uma lista de ferramentas. Começa pelo que você está sentindo.
        </p>
      </section>

      <section className="mx-auto max-w-[1168px] px-6 pb-24">
        {STEPS.map((s) => (
          <div key={s.n} className="grid grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-x-16 gap-y-6 border-t border-[#E7E0EC] py-12">
            <div>
              <div className="text-[14px] font-semibold text-[#5B21D6]">{s.n}</div>
              <h2 className="m-0 mt-[10px] text-[30px] font-bold leading-9 tracking-[-0.5px]">{s.t}</h2>
            </div>
            <div>
              <p className="m-0 text-pretty text-[18px] leading-7 text-[#49454F]">{s.d}</p>
              <div className="mt-5 rounded-2xl bg-[#F8F5FB] px-5 py-[18px]">
                <div className="text-[12px] font-medium text-[#49454F]">{s.ex}</div>
                <div className="mt-[6px] text-[16px] font-medium leading-6">{s.exv}</div>
              </div>
            </div>
          </div>
        ))}
      </section>

      <section className="bg-[#F8F5FB] px-6 py-24">
        <div className="mx-auto grid max-w-[1120px] grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-14">
          <h2 className="m-0 text-balance text-[clamp(28px,3.4vw,40px)] font-bold leading-[1.1] tracking-[-1px]">O que o app pode analisar</h2>
          <div className="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-x-10 gap-y-7">
            {TOOLS.map((t) => (
              <div key={t.t}>
                <div className="text-[17px] font-semibold">{t.t}</div>
                <div className="mt-1 text-[15px] leading-[22px] text-[#49454F]">{t.d}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="px-6 pb-28 pt-24 text-center">
        <h2 className="mx-auto m-0 max-w-[14em] text-balance text-[clamp(28px,3.6vw,42px)] font-bold leading-[1.1] tracking-[-1px]">
          Pronto para descobrir o que está acontecendo?
        </h2>
        <Link href="/#baixar" className="mt-8 inline-block rounded-full bg-[#5B21D6] px-8 py-4 text-[16px] font-medium text-white no-underline">
          Baixar no Google Play
        </Link>
      </section>
    </div>
  )
}

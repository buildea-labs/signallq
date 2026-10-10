const STEPS = [
  { n: '01', q: 'O que está acontecendo?', a: 'Você diz o que sente: lento, travando, caindo. A análise começa pelo sintoma.' },
  { n: '02', q: 'Onde está o problema?', a: 'Wi-Fi, roteador, operadora ou DNS. O app testa só o que importa.' },
  { n: '03', q: 'O que eu faço agora?', a: 'Um próximo passo curto e seguro, sem precisar entender de rede.' },
  { n: '04', q: 'Resolveu?', a: 'Verifique de novo e veja se melhorou.' },
];

export function HowItWorksSteps() {
  return (
    <section id="como" className="bg-[#F8F5FB] px-6 py-24">
      <div className="mx-auto max-w-[1120px]">
        <h2 className="m-0 max-w-[16em] text-balance text-[clamp(30px,3.8vw,42px)] font-bold leading-[1.1] tracking-[-1px]">
          Quatro perguntas. Uma resposta de cada vez.
        </h2>
        <div className="mt-14 grid grid-cols-[repeat(auto-fit,minmax(220px,1fr))] gap-10">
          {STEPS.map((s) => (
            <div key={s.n} className="border-t-2 border-[#1C1B1F] pt-5">
              <div className="text-[14px] font-semibold text-[#5B21D6]">{s.n}</div>
              <div className="mt-3 text-[22px] font-semibold leading-7">{s.q}</div>
              <p className="mt-3 text-[16px] leading-6 text-[#49454F]">{s.a}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

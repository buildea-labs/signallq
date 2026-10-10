const STEPS = [
  { n: '01', q: 'O que está acontecendo?', a: 'Você diz o que sente: lento, travando, caindo. A análise começa pelo sintoma.' },
  { n: '02', q: 'Onde está o problema?', a: 'Wi-Fi, roteador, operadora ou DNS. O app testa só o que importa.' },
  { n: '03', q: 'O que eu faço agora?', a: 'Um próximo passo curto e seguro, sem precisar entender de rede.' },
  { n: '04', q: 'Resolveu?', a: 'Verifique de novo e veja se melhorou.' },
];

export function HowItWorksSteps() {
  return (
    <div className="sq-app-reveal flex flex-col gap-6">
      <h2 className="m-0 text-center font-bold text-[26px] leading-[32px] text-[color:var(--text-primary)] font-sans">
        Quatro perguntas. Uma resposta de cada vez.
      </h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 w-full max-w-[1080px] mx-auto">
        {STEPS.map((s) => (
          <div key={s.n} className="border-t-2 border-[color:var(--text-primary)] pt-3">
            <div className="font-semibold text-[13px] text-[color:var(--accent)] font-sans">{s.n}</div>
            <div className="mt-2 font-semibold text-[17px] leading-[22px] text-[color:var(--text-primary)] font-sans">{s.q}</div>
            <p className="mt-2 font-normal text-[14px] leading-[20px] text-[color:var(--text-secondary)] font-sans">{s.a}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

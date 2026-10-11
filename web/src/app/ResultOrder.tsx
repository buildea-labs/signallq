const ORDER = [
  { n: '1', t: 'Veredito', d: 'O que está acontecendo, em uma frase.' },
  { n: '2', t: 'Causa provável', d: 'Com nível de confiança. Hipótese não vira certeza.' },
  { n: '3', t: 'Evidências', d: 'Só as essenciais, cada uma explicada.' },
  { n: '4', t: 'Ação recomendada', d: 'O passo mais útil agora.' },
  { n: '5', t: 'Confirmação', d: 'Nova verificação para ver se funcionou.' },
];

export function ResultOrder() {
  return (
    <section
      id="resultado"
      className="mx-auto grid w-full max-w-[1168px] scroll-mt-20 grid-cols-[repeat(auto-fit,minmax(320px,1fr))] items-center gap-[72px] px-6 py-28"
    >
      <div>
        <h2 className="m-0 text-balance text-[clamp(30px,3.8vw,42px)] font-bold leading-[1.1] tracking-[-1px]">
          Primeiro a conclusão. Os números vêm depois.
        </h2>
        <p className="mt-5 text-pretty text-[18px] leading-7 text-[#49454F]">
          Cada resultado segue a mesma ordem, em palavras simples. Detalhe técnico só aparece se você quiser.
        </p>
      </div>
      <ol className="m-0 flex list-none flex-col p-0">
        {ORDER.map((o) => (
          <li key={o.n} className="flex items-baseline gap-5 border-b border-[#F3EEFA] py-5">
            <span className="w-6 shrink-0 text-[14px] font-semibold text-[#5B21D6]">{o.n}</span>
            <div>
              <div className="text-[18px] font-semibold">{o.t}</div>
              <div className="mt-1 text-[15px] leading-[22px] text-[#49454F]">{o.d}</div>
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}

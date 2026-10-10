const ORDER = [
  { n: '1', t: 'Veredito', d: 'O que está acontecendo, em uma frase.' },
  { n: '2', t: 'Causa provável', d: 'Com nível de confiança. Hipótese não vira certeza.' },
  { n: '3', t: 'Evidências', d: 'Só as essenciais, cada uma explicada.' },
  { n: '4', t: 'Ação recomendada', d: 'O passo mais útil agora.' },
  { n: '5', t: 'Confirmação', d: 'Nova verificação para ver se funcionou.' },
];

export function ResultOrder() {
  return (
    <div className="sq-app-reveal grid grid-cols-1 lg:grid-cols-2 gap-10 w-full max-w-[1080px] mx-auto items-start">
      <div>
        <h2 className="m-0 font-bold text-[26px] leading-[32px] text-[color:var(--text-primary)] font-sans">
          Primeiro a conclusão. Os números vêm depois.
        </h2>
        <p className="mt-3 font-normal text-[15px] leading-[22px] text-[color:var(--text-secondary)] font-sans">
          Cada resultado segue a mesma ordem, em palavras simples. Detalhe técnico só aparece se você quiser.
        </p>
      </div>
      <ol className="m-0 flex list-none flex-col p-0">
        {ORDER.map((o) => (
          <li key={o.n} className="flex gap-4 items-baseline py-3 border-b border-[color:var(--border)]">
            <span className="font-semibold text-[13px] text-[color:var(--accent)] w-5 shrink-0 font-sans">{o.n}</span>
            <div>
              <div className="font-semibold text-[16px] text-[color:var(--text-primary)] font-sans">{o.t}</div>
              <div className="mt-1 font-normal text-[14px] leading-[20px] text-[color:var(--text-secondary)] font-sans">{o.d}</div>
            </div>
          </li>
        ))}
      </ol>
    </div>
  );
}

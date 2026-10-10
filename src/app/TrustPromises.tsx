const PROMISES = [
  { t: 'Cada número é explicado', d: 'Nenhuma métrica aparece sem dizer o que significa para você.' },
  { t: 'Honesto sobre incertezas', d: 'Quando há só um indício, o app diz que é um indício.' },
  { t: 'Toda descoberta leva a uma ação', d: 'Você sai da análise sabendo o que tentar.' },
];

export function TrustPromises() {
  return (
    <div className="sq-app-reveal w-full bg-black text-[#F5F2F7] rounded-[16px] px-6 py-10 sm:px-10 sm:py-14">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-10 w-full max-w-[1080px] mx-auto">
        <h2 className="m-0 font-bold text-[26px] leading-[32px] font-sans text-balance">
          Sem achismo. Sem jargão.
        </h2>
        <div className="flex flex-col gap-6">
          {PROMISES.map((p) => (
            <div key={p.t}>
              <div className="font-semibold text-[17px] font-sans">{p.t}</div>
              <p className="mt-1 font-normal text-[14px] leading-[20px] text-[#CAC4D0] font-sans">{p.d}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

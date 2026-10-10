const PROMISES = [
  { t: 'Cada número é explicado', d: 'Nenhuma métrica aparece sem dizer o que significa para você.' },
  { t: 'Honesto sobre incertezas', d: 'Quando há só um indício, o app diz que é um indício.' },
  { t: 'Toda descoberta leva a uma ação', d: 'Você sai da análise sabendo o que tentar.' },
];

export function TrustPromises() {
  return (
    <section className="bg-black px-6 py-24 text-[#F5F2F7]">
      <div className="mx-auto grid max-w-[1120px] grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-14">
        <h2 className="m-0 text-balance text-[clamp(30px,3.8vw,42px)] font-bold leading-[1.1] tracking-[-1px]">
          Sem achismo. Sem jargão.
        </h2>
        <div className="flex flex-col gap-7">
          {PROMISES.map((p) => (
            <div key={p.t}>
              <div className="text-[20px] font-semibold">{p.t}</div>
              <p className="mt-2 text-[16px] leading-6 text-[#CAC4D0]">{p.d}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

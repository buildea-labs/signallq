import { AccessibleAccordion } from "@/components/institutional/InstitutionalFoundation";

const FAQ = [
  { title: 'O SignallQ é só um teste de velocidade?', content: 'Não. Ele mede quando necessário, mas o foco é explicar a causa do problema e orientar a solução.' },
  { title: 'Preciso entender de redes?', content: 'Não. O resultado usa frases curtas e palavras comuns. O detalhe técnico é opcional.' },
  { title: 'Funciona em iPhone?', content: 'Por enquanto o app de diagnóstico é para Android.' },
  { title: 'Quanto custa?', content: 'Você pode começar de graça, sem cadastro.' },
];

export function LandingFaq() {
  return (
    <div className="sq-app-reveal w-full max-w-[720px] mx-auto">
      <h2 className="m-0 mb-6 font-bold text-[26px] leading-[32px] text-[color:var(--text-primary)] font-sans">
        Dúvidas comuns
      </h2>
      <AccessibleAccordion items={FAQ} />
    </div>
  );
}

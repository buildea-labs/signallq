import { FaqList } from '@/components/FaqList'

const GROUPS = [
  {
    t: 'Sobre o app',
    items: [
      { q: 'O SignallQ é só um teste de velocidade?', a: 'Não. Ele mede quando necessário, mas o foco é explicar a causa do problema e orientar a solução.' },
      { q: 'Preciso entender de redes?', a: 'Não. O resultado usa frases curtas e palavras comuns. O detalhe técnico é opcional.' },
      { q: 'Funciona em iPhone?', a: 'Por enquanto o app de diagnóstico é só para Android.' },
      { q: 'Quanto custa?', a: 'O app é gratuito, sem assinatura e sem compras dentro dele. Ele exibe anúncios do Google AdMob.' },
    ],
  },
  {
    t: 'Diagnóstico',
    items: [
      { q: 'O diagnóstico por IA resolve meu problema?', a: 'Ele é um apoio: analisa dados técnicos e recomenda ações com base em padrões conhecidos. Não é parecer profissional nem garantia. Se o problema persistir, fale com sua operadora.' },
      { q: 'Por que o app pede permissão de localização?', a: 'O Android exige essa permissão para liberar o nome e o canal do Wi-Fi. O app não rastreia sua localização por GPS.' },
      { q: 'As medições são sempre exatas?', a: 'Não. Velocidade varia com o momento e a rede, por isso o app indica o nível de confiança do resultado.' },
    ],
  },
  {
    t: 'Privacidade',
    items: [
      { q: 'O SignallQ coleta meus dados pessoais?', a: 'Não coletamos nome, e-mail, endereço nem localização GPS. O histórico fica no seu aparelho. Detalhes na Política de Privacidade.' },
      { q: 'Posso apagar meus dados?', a: 'Sim. Limpe o histórico em Ajustes, revogue o consentimento em Ajustes → Privacidade ou desinstale o app.' },
    ],
  },
]

const FAQ_JSON_LD = {
  '@context': 'https://schema.org',
  '@type': 'FAQPage',
  mainEntity: GROUPS.flatMap((g) =>
    g.items.map((item) => ({ '@type': 'Question', name: item.q, acceptedAnswer: { '@type': 'Answer', text: item.a } }))
  ),
}

export default function DuvidasContent() {
  return (
    <section className="mx-auto max-w-[808px] bg-white px-6 pb-[120px] pt-[88px] font-sans leading-[normal] text-[#1C1B1F]">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(FAQ_JSON_LD) }} />
      <div className="mb-5 text-[14px] font-medium text-[#5B21D6]">Dúvidas</div>
      <h1 className="m-0 mb-12 text-balance text-[clamp(36px,4.6vw,52px)] font-bold leading-[1.05] tracking-[-1.2px]">
        Perguntas comuns sobre o SignallQ
      </h1>
      {GROUPS.map((g) => (
        <div key={g.t} className="mb-12">
          <div className="mb-2 text-[14px] font-semibold text-[#49454F]">{g.t}</div>
          <FaqList items={g.items} />
        </div>
      ))}
      <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-4 rounded-[20px] bg-[#F8F5FB] p-7">
        <div>
          <div className="text-[18px] font-semibold">Não achou sua dúvida?</div>
          <div className="mt-1 text-[15px] text-[#49454F]">Escreva para a gente.</div>
        </div>
        <a href="mailto:suporte@signallq.com" className="rounded-full bg-[#5B21D6] px-6 py-[13px] text-[15px] font-medium text-white no-underline">
          suporte@signallq.com
        </a>
      </div>
    </section>
  )
}

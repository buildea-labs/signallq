import { Banda } from '@/components/Banda'
import {
  AccessibleAccordion,
  InstitutionalHero,
  ReadingLayout,
} from '@/components/institutional/InstitutionalFoundation'

const GROUPS = [
  {
    title: 'Sobre o app',
    items: [
      { title: 'O SignallQ é só um teste de velocidade?', content: 'Não. Ele mede quando necessário, mas o foco é explicar a causa do problema e orientar a solução.' },
      { title: 'Preciso entender de redes?', content: 'Não. O resultado usa frases curtas e palavras comuns. O detalhe técnico é opcional.' },
      { title: 'Funciona em iPhone?', content: 'Por enquanto o app de diagnóstico é só para Android.' },
      { title: 'Quanto custa?', content: 'O app é gratuito, sem assinatura e sem compras dentro dele. Ele exibe anúncios do Google AdMob.' },
    ],
  },
  {
    title: 'Diagnóstico',
    items: [
      { title: 'O diagnóstico por IA resolve meu problema?', content: 'Ele é um apoio: analisa dados técnicos e recomenda ações com base em padrões conhecidos. Não é parecer profissional nem garantia. Se o problema persistir, fale com sua operadora.' },
      { title: 'Por que o app pede permissão de localização?', content: 'O Android exige essa permissão para liberar o nome e o canal do Wi-Fi. O app não rastreia sua localização por GPS.' },
      { title: 'As medições são sempre exatas?', content: 'Não. Velocidade varia com o momento e a rede, por isso o app indica o nível de confiança do resultado.' },
    ],
  },
  {
    title: 'Privacidade',
    items: [
      { title: 'O SignallQ coleta meus dados pessoais?', content: 'Não coletamos nome, e-mail, endereço nem localização GPS. O histórico fica no seu aparelho. Detalhes na Política de Privacidade.' },
      { title: 'Posso apagar meus dados?', content: 'Sim. Limpe o histórico em Ajustes, revogue o consentimento em Ajustes → Privacidade ou desinstale o app.' },
    ],
  },
]

const FAQ_JSON_LD = {
  '@context': 'https://schema.org',
  '@type': 'FAQPage',
  mainEntity: GROUPS.flatMap((g) =>
    g.items.map((item) => ({
      '@type': 'Question',
      name: item.title,
      acceptedAnswer: { '@type': 'Answer', text: item.content },
    }))
  ),
}

export default function DuvidasContent() {
  return (
    <Banda className="py-8 md:py-12 lg:py-16">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(FAQ_JSON_LD) }} />
      <ReadingLayout className="flex flex-col gap-10">
        <InstitutionalHero overline="Dúvidas" title="Perguntas comuns sobre o SignallQ" />
        {GROUPS.map((group) => (
          <AccessibleAccordion key={group.title} title={group.title} items={group.items} />
        ))}
        <section className="flex flex-wrap items-center justify-between gap-4 rounded-[var(--radius-card)] px-6 py-6" style={{ background: 'var(--bg-secondary)' }}>
          <div>
            <div className="title-medium m-0">Não achou sua dúvida?</div>
            <div className="body-small mt-1" style={{ color: 'var(--text-secondary)' }}>Escreva para a gente.</div>
          </div>
          <a
            href="mailto:suporte@signallq.com"
            className="label-large flex h-10 items-center justify-center rounded-[var(--radius-button)] px-5 no-underline"
            style={{ background: 'var(--accent)', color: 'var(--on-accent)' }}
          >
            suporte@signallq.com
          </a>
        </section>
      </ReadingLayout>
    </Banda>
  )
}

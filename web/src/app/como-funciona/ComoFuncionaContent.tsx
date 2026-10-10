import { Banda } from '@/components/Banda'
import { ConnectionIllustration } from '@/components/institutional/InstitutionalIllustrations'
import {
  InstitutionalCta,
  InstitutionalHero,
  ReadingLayout,
  StepsBlock,
  type InstitutionalStep,
} from '@/components/institutional/InstitutionalFoundation'

const STEPS: InstitutionalStep[] = [
  {
    title: 'Entender',
    description: (
      <>
        <p>Você escolhe o que está sentindo: lentidão, travadas, quedas ou jogo com atraso. O sintoma define quais análises vão rodar.</p>
        <p className="body-small mt-2" style={{ color: 'var(--text-tertiary)' }}>Você diz: &ldquo;A internet fica lenta à noite.&rdquo;</p>
      </>
    ),
  },
  {
    title: 'Diagnosticar',
    description: (
      <>
        <p>O app mede só o necessário: Wi-Fi, velocidade, DNS, rede móvel. Sem menus técnicos para explorar.</p>
        <p className="body-small mt-2" style={{ color: 'var(--text-tertiary)' }}>Em andamento: verificando sinal, velocidade e DNS…</p>
      </>
    ),
  },
  {
    title: 'Resolver',
    description: (
      <>
        <p>Você recebe a causa provável com nível de confiança, as evidências essenciais e uma ação recomendada.</p>
        <p className="body-small mt-2" style={{ color: 'var(--text-tertiary)' }}>Ação recomendada: troque o roteador de lugar, longe de paredes e do micro-ondas.</p>
      </>
    ),
  },
  {
    title: 'Confirmar',
    description: (
      <>
        <p>Depois da ação, o app repete a verificação relevante e mostra se houve melhora.</p>
        <p className="body-small mt-2" style={{ color: 'var(--text-tertiary)' }}>Nova verificação: sinal no cômodo, de Fraco para Bom.</p>
      </>
    ),
  },
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
    <Banda className="py-8 md:py-12 lg:py-16">
      <ReadingLayout className="flex flex-col gap-10">
        <InstitutionalHero
          overline="Como funciona"
          title="Do sintoma à solução, em quatro passos."
          summary="O SignallQ não começa por uma lista de ferramentas. Começa pelo que você está sentindo."
          illustration={<ConnectionIllustration />}
        />

        <StepsBlock steps={STEPS} />

        <section className="flex flex-col gap-4">
          <h2 className="title-large m-0">O que o app pode analisar</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-5">
            {TOOLS.map((tool) => (
              <div key={tool.t}>
                <div className="title-medium m-0">{tool.t}</div>
                <div className="body-small mt-1" style={{ color: 'var(--text-secondary)' }}>{tool.d}</div>
              </div>
            ))}
          </div>
        </section>

        <InstitutionalCta label="Fazer o teste de velocidade" href="/teste-de-velocidade" />
      </ReadingLayout>
    </Banda>
  )
}

import { Banda } from "@/components/Banda";
import type { Metadata } from 'next'
import {
  AccessibleAccordion,
  HighlightSection,
  InformationGroup,
  InstitutionalHero,
  ReadingLayout,
} from '../../components/institutional/InstitutionalFoundation'
import { TermsIllustration } from '../../components/institutional/InstitutionalIllustrations'
import { PAGE_META } from '../../lib/pageMetaCatalog'
import { routeMetadata } from '../../lib/routeMetadata'

export const metadata: Metadata = routeMetadata(PAGE_META['/termos'])

const SECTIONS = [
  {
    title: '1. Aceitação dos termos',
    text: 'Ao usar o aplicativo SignallQ, você concorda com estes Termos de Uso. Se não concordar, não utilize o aplicativo.',
  },
  {
    title: '2. Descrição do serviço',
    text: 'O SignallQ é um aplicativo Android que mede velocidade, sinal Wi-Fi, sinal móvel e latência, e oferece diagnóstico de causa provável para problemas de conexão.',
  },
  {
    title: '3. Uso permitido',
    text: 'Você pode medir e entender sua própria conexão. Não pode usar o aplicativo para atacar, sobrecarregar ou interferir na infraestrutura de medição, nem para fins ilegais.',
  },
  {
    title: '4. Gratuidade',
    text: 'O download e as funcionalidades básicas do aplicativo são gratuitos. O app pode exibir anúncios quando configurado, conforme descrito na Política de Privacidade.',
  },
  {
    title: '5. Disponibilidade',
    text: 'O serviço é fornecido "como está". Não garantimos disponibilidade ininterrupta nem precisão absoluta: a medição depende de infraestrutura de terceiros (Cloudflare, Google).',
  },
  {
    title: '6. Privacidade',
    text: 'O tratamento dos seus dados é regido pela nossa Política de Privacidade, disponível em /privacidade.',
  },
  {
    title: '7. Propriedade intelectual',
    text: 'O SignallQ, incluindo código, design, marca e conteúdo, é propriedade da Buildea. Todos os direitos reservados.',
  },
  {
    title: '8. Limitação de responsabilidade',
    text: 'A Buildea não se responsabiliza por danos decorrentes do uso do aplicativo, de decisões tomadas com base nos resultados ou de indisponibilidade temporária.',
  },
  {
    title: '9. Alterações nos termos',
    text: 'A Buildea pode atualizar estes Termos a qualquer momento. O uso continuado após alterações implica aceitação.',
  },
  {
    title: '10. Legislação aplicável',
    text: 'Estes Termos são regidos pelas leis brasileiras, em conformidade com a LGPD (Lei 13.709/2018) e o Marco Civil da Internet (Lei 12.965/2014).',
  },
  {
    title: '11. Contato',
    text: 'Para dúvidas sobre estes Termos: suporte@signallq.com (Buildea).',
  },
]

export default function Page() {
  return (
    <Banda className="py-8 md:py-12 lg:py-16">
      <ReadingLayout className="flex flex-col gap-7">
        <InstitutionalHero overline="Termos de Uso" title="Termos de Uso do SignallQ" summary="As regras para usar o aplicativo SignallQ." meta="Última atualização: 27 de agosto de 2026" illustration={<TermsIllustration />} />
        <HighlightSection title="Resumo direto">
          <p>O download e o uso básico do app são gratuitos. Use o serviço para entender a própria conexão, sem atacar ou sobrecarregar a infraestrutura. Resultados descrevem uma medição e não são garantia de disponibilidade ou velocidade contratada.</p>
        </HighlightSection>
        <InformationGroup title="Pontos importantes" items={[
          { label: 'Uso aceitável', value: 'Medir e entender a própria conexão e usar o conteúdo dentro da lei.' },
          { label: 'Limites', value: 'O serviço é fornecido como está e depende de infraestrutura de terceiros, inclusive Cloudflare e Google.' },
          { label: 'Privacidade', value: 'O tratamento de dados é explicado na Política de Privacidade.' },
          { label: 'Contato', value: <a href="mailto:suporte@signallq.com">suporte@signallq.com</a> },
        ]} />
        <AccessibleAccordion title="Texto completo" items={SECTIONS.map((section, index) => ({
          title: section.title,
          content: <p>{section.text}</p>,
          defaultOpen: index === 0 || index === 5 || index === 10,
        }))} />
      </ReadingLayout>
    </Banda>
  )
}

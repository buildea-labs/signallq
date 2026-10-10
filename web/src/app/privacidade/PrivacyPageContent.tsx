import { Banda } from "@/components/Banda";
import { ShieldCheck } from 'lucide-react'
import {
  AccessibleAccordion,
  HighlightSection,
  IllustrationWrapper,
  InformationGroup,
  InstitutionalHero,
  ReadingLayout,
} from '../../components/institutional/InstitutionalFoundation'

export default function PrivacyPageContent() {
  return (
    <Banda className="py-8 md:py-12 lg:py-16">
      <ReadingLayout className="flex flex-col gap-7">
        <InstitutionalHero
          overline="Privacidade"
          title="Política de Privacidade do SignallQ"
          summary="Leia o que o aplicativo Android trata, o que fica no aparelho e quando há envio a serviços externos."
          meta="Versão 2.0 · Atualizada em 27 de agosto de 2026"
          illustration={<IllustrationWrapper><ShieldCheck size={48} strokeWidth={1.5} /></IllustrationWrapper>}
        />

        <HighlightSection title="Resumo direto">
          <p>O SignallQ trata dados técnicos para medir e explicar a conexão do seu aparelho Android. Esta página separa o que fica só no aparelho do que é enviado, com consentimento, a serviços externos, sem prometer anonimato absoluto.</p>
        </HighlightSection>

        <section aria-labelledby="common-policy" className="flex flex-col gap-4">
          <h2 id="common-policy" className="title-large m-0">O que tratamos</h2>
          <InformationGroup items={[
            { label: 'Finalidade', value: 'Executar medições e diagnósticos de conexão e, quando houver consentimento, melhorar confiabilidade, produto e publicidade.' },
            { label: 'Bases legais', value: 'Execução das funcionalidades solicitadas, consentimento quando exigido e legítimo interesse para segurança e melhoria, sempre conforme a legislação aplicável.' },
            { label: 'Papéis e terceiros', value: 'O SignallQ define a finalidade do seu produto. Provedores como Google, Firebase e Cloudflare tratam dados conforme seus serviços e políticas; o papel jurídico exato pode variar pelo serviço e contrato.' },
            { label: 'Segurança e retenção', value: 'Dados locais permanecem até você excluí-los ou desinstalar o aplicativo. O código auditado não define um prazo único para dados remotos; eles seguem a configuração e as políticas dos provedores aplicáveis.' },
          ]} />
        </section>

        <section aria-labelledby="app-details" className="flex flex-col gap-5">
          <h2 id="app-details" className="title-large m-0">No aplicativo Android</h2>
          <p className="body-medium m-0 text-pretty">O app mede a conexão e recursos de rede do aparelho. Resultados, preferências e perfis ficam no dispositivo; alguns envios só ocorrem conforme o consentimento e o recurso usado.</p>
          <InformationGroup items={[
            { label: 'No aparelho', value: 'Medições, diagnósticos, preferências, perfis de conexão e dados de rede usados pelo app são persistidos em bancos Room/SQLite e DataStore.' },
            { label: 'Permissões', value: 'Internet e estado da rede; Wi‑Fi e localização para recursos de Wi‑Fi; telefonia para métricas móveis quando solicitada; notificações para alertas. O uso depende do recurso e da permissão concedida.' },
            { label: 'Enviado com consentimento', value: 'Eventos de uso, resultados de diagnóstico não contaminados, identificador anônimo do dispositivo, modelo, versão do Android, versão e canal do app podem seguir para Firebase e para o Worker administrativo do SignallQ.' },
            { label: 'Anúncios', value: 'O Google Mobile Ads/AdMob só pode receber pedido de anúncio após o fluxo UMP aplicável. Configuração remota de anúncios usa Firebase Remote Config.' },
          ]} />
          <AccessibleAccordion title="Detalhes técnicos" items={[
            { title: 'Medição, diagnóstico e infraestrutura', content: 'A medição troca tráfego com serviços de rede. O diagnóstico remoto e o ingest usam Workers da Cloudflare quando o recurso aplicável é executado. IPs e metadados técnicos de conexão podem ser processados transitoriamente pela infraestrutura de rede necessária à requisição; isso não equivale a afirmar que o SignallQ os armazena.' },
            { title: 'Analytics e falhas', content: 'O código integra Firebase Analytics e Firebase Crashlytics. O Analytics registra eventos, identificador de sessão e propriedades de ambiente, canal de distribuição e tipo de build; o Crashlytics pode receber dados técnicos de falha conforme o SDK da Firebase. A política não promete anonimato absoluto.' },
            { title: 'Excluir, exportar e controlar', content: 'Em Ajustes > Privacidade, o app oferece limpar histórico, apagar dados locais e resetar o app, com confirmação. No Histórico, as medições podem ser exportadas por período em CSV ou PDF e compartilhadas pelo sistema Android; o arquivo é gerado temporariamente no cache. O consentimento LGPD pode ser alterado em Ajustes > Privacidade; o consentimento de anúncios é administrado pelo fluxo UMP quando aplicável.' },
          ]} />
        </section>

        <AccessibleAccordion title="Direitos, contato e alterações" items={[
          { title: 'Seus direitos e como falar conosco', defaultOpen: true, content: <p>Você pode pedir confirmação de tratamento, acesso, correção, anonimização, bloqueio, eliminação, portabilidade, informação sobre compartilhamento e revisão de consentimento, conforme a LGPD e os limites aplicáveis. Para dúvidas ou solicitações, escreva para <a href="mailto:suporte@signallq.com">suporte@signallq.com</a>.</p> },
          { title: 'Histórico de alterações', defaultOpen: true, content: <p>Versão 2.0 (27 de agosto de 2026): política simplificada para cobrir somente o aplicativo Android, após o site público passar a ser a landing de divulgação do app. Versão 1.0 (1º de agosto de 2026): política unificada criada após auditoria do código Android e Web/PWA. Mudanças relevantes serão registradas nesta seção com a nova data de versão.</p> },
        ]} />
      </ReadingLayout>
    </Banda>
  )
}

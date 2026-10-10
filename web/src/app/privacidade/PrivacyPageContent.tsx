import { LegalPage, type LegalSection } from '../../components/LegalPage'

// Texto jurídico preservado integralmente (auditado contra o código Android);
// só a apresentação segue o layout do protótipo.
const Item = ({ label, children }: { label: string; children: string }) => (
  <>
    <strong className="font-semibold text-[#1C1B1F]">{label}.</strong> {children}
  </>
)

const SECTIONS: LegalSection[] = [
  {
    t: '1. O que tratamos',
    ps: [
      <Item key="f" label="Finalidade">Executar medições e diagnósticos de conexão e, quando houver consentimento, melhorar confiabilidade, produto e publicidade.</Item>,
      <Item key="b" label="Bases legais">Execução das funcionalidades solicitadas, consentimento quando exigido e legítimo interesse para segurança e melhoria, sempre conforme a legislação aplicável.</Item>,
      <Item key="p" label="Papéis e terceiros">O SignallQ define a finalidade do seu produto. Provedores como Google, Firebase e Cloudflare tratam dados conforme seus serviços e políticas; o papel jurídico exato pode variar pelo serviço e contrato.</Item>,
      <Item key="s" label="Segurança e retenção">Dados locais permanecem até você excluí-los ou desinstalar o aplicativo. O código auditado não define um prazo único para dados remotos; eles seguem a configuração e as políticas dos provedores aplicáveis.</Item>,
    ],
  },
  {
    t: '2. No aplicativo Android',
    ps: [
      'O app mede a conexão e recursos de rede do aparelho. Resultados, preferências e perfis ficam no dispositivo; alguns envios só ocorrem conforme o consentimento e o recurso usado.',
      <Item key="a" label="No aparelho">Medições, diagnósticos, preferências, perfis de conexão e dados de rede usados pelo app são persistidos em bancos Room/SQLite e DataStore.</Item>,
      <Item key="b" label="Permissões">Internet e estado da rede; Wi‑Fi e localização para recursos de Wi‑Fi; telefonia para métricas móveis quando solicitada; notificações para alertas. O uso depende do recurso e da permissão concedida.</Item>,
      <Item key="c" label="Enviado com consentimento">Eventos de uso, resultados de diagnóstico não contaminados, identificador anônimo do dispositivo, modelo, versão do Android, versão e canal do app podem seguir para Firebase e para o Worker administrativo do SignallQ.</Item>,
      <Item key="d" label="Anúncios">O Google Mobile Ads/AdMob só pode receber pedido de anúncio após o fluxo UMP aplicável. Configuração remota de anúncios usa Firebase Remote Config.</Item>,
      <Item key="e" label="Medição, diagnóstico e infraestrutura">A medição troca tráfego com serviços de rede. O diagnóstico remoto e o ingest usam Workers da Cloudflare quando o recurso aplicável é executado. IPs e metadados técnicos de conexão podem ser processados transitoriamente pela infraestrutura de rede necessária à requisição; isso não equivale a afirmar que o SignallQ os armazena.</Item>,
      <Item key="f" label="Analytics e falhas">O código integra Firebase Analytics e Firebase Crashlytics. O Analytics registra eventos, identificador de sessão e propriedades de ambiente, canal de distribuição e tipo de build; o Crashlytics pode receber dados técnicos de falha conforme o SDK da Firebase. A política não promete anonimato absoluto.</Item>,
      <Item key="g" label="Excluir, exportar e controlar">Em Ajustes &gt; Privacidade, o app oferece limpar histórico, apagar dados locais e resetar o app, com confirmação. No Histórico, as medições podem ser exportadas por período em CSV ou PDF e compartilhadas pelo sistema Android; o arquivo é gerado temporariamente no cache. O consentimento LGPD pode ser alterado em Ajustes &gt; Privacidade; o consentimento de anúncios é administrado pelo fluxo UMP quando aplicável.</Item>,
    ],
  },
  {
    t: '3. Seus direitos e como falar conosco',
    ps: [
      <>
        Você pode pedir confirmação de tratamento, acesso, correção, anonimização, bloqueio, eliminação, portabilidade, informação sobre compartilhamento e revisão de consentimento, conforme a LGPD e os limites aplicáveis. Para dúvidas ou solicitações, escreva para <a href="mailto:suporte@signallq.com">suporte@signallq.com</a>.
      </>,
    ],
  },
  {
    t: '4. Histórico de alterações',
    ps: [
      'Versão 2.0 (27 de agosto de 2026): política simplificada para cobrir somente o aplicativo Android, após o site público passar a ser a landing de divulgação do app. Versão 1.0 (1º de agosto de 2026): política unificada criada após auditoria do código Android e Web/PWA. Mudanças relevantes serão registradas nesta seção com a nova data de versão.',
    ],
  },
]

export default function PrivacyPageContent() {
  return (
    <LegalPage
      title="Política de Privacidade"
      updated="Versão 2.0 · Atualizada em 27 de agosto de 2026"
      intro="Leia o que o aplicativo Android trata, o que fica no aparelho e quando há envio a serviços externos."
      summary={{
        title: 'Resumo direto',
        text: 'O SignallQ trata dados técnicos para medir e explicar a conexão do seu aparelho Android. Esta página separa o que fica só no aparelho do que é enviado, com consentimento, a serviços externos, sem prometer anonimato absoluto.',
      }}
      sections={SECTIONS}
      sectionGap="mt-12"
    />
  )
}

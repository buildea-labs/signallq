# Auditoria de dados — política unificada

Data da auditoria: 1º de agosto de 2026. Fontes: código atual de `signallq-web` e de `C:\Projetos\SignallQ\android`. Esta é uma descrição técnica comprovada pelo código, não substitui revisão jurídica dos contratos dos fornecedores.

## Web/PWA

| Área | Evidência no código | Tratamento confirmado |
| --- | --- | --- |
| Medição | `src/lib/config.ts`, `src/lib/speedEngine.ts` | O navegador chama `speed.cloudflare.com`, `cloudflare-dns.com` e, quando configurado, Worker de latência. A infraestrutura que responde recebe IP e metadados técnicos da requisição. |
| Histórico | `src/lib/historyDatabase.ts`, `src/lib/measurementRepository.ts`, `src/lib/comparisonRepository.ts`, `src/lib/historyExport.ts`, `src/lib/historySelectors.ts` | IndexedDB `signallq-site-history`, versão 3: medições, metadados declarados e comparações. Sem sincronização. Exclusão, limpeza e exportação são locais. |
| Navegador | `src/lib/adConsent.ts`, `src/hooks/usePwaInstall.ts`, `src/hooks/useSpeedTest.ts`, `src/lib/telemetry.ts`, `src/lib/measurementSessionStore.ts` | localStorage: consentimento de anúncios, descarte do prompt PWA e lock temporário; sessionStorage: UUID de telemetria e `signallq_measurement_session_v1` (contexto de entrada, respostas e estado do questionário da medição atual). |
| Telemetria | `src/lib/telemetry.ts`, `src/app/api/track/route.ts` | Eventos de uso seguem para `/api/track`; se `SITE_INGEST_KEY` existir, Vercel encaminha ao Worker administrativo Cloudflare. |
| Publicidade | `src/components/AdSenseScript.tsx` | Google AdSense só é carregado se houver ID público configurado e consentimento aceito. Não há cookie próprio identificado no código. |
| PWA | `src/app/sw.ts` | Service worker Serwist usa cache de recursos. |
| Não identificado | busca no repositório Web | Nenhuma integração de Firebase, Sentry, Crashlytics ou script Cloudflare Web Analytics foi identificada. |

## Android

| Área | Evidência no código | Tratamento confirmado |
| --- | --- | --- |
| Permissões | `android/app/src/main/AndroidManifest.xml` | Internet, estado da rede, Wi‑Fi, localização fina/aproximada, dispositivos Wi‑Fi próximos, telefonia, notificações e multicast Wi‑Fi. |
| Dados locais | `core/database`, `core/datastore`, `PrivacidadeScreen.kt` | Room/SQLite e DataStore guardam medições, diagnósticos, preferências e perfis. A tela permite limpar histórico, apagar dados locais e resetar. |
| Exportação Android | `HistoricoScreen.kt`, `ExportHistoricoBottomSheet.kt` | O Histórico exporta medições por período em CSV ou PDF, cria arquivo temporário em cache e abre o compartilhamento do Android por FileProvider. |
| Consentimento | `LgpdConsentDialog.kt`, `PreferenciasAppRepository.kt`, `ConsentManager.kt` | Consentimento LGPD controla analytics/telemetria; UMP controla a possibilidade de pedir anúncios. |
| Analytics e falhas | `FirebaseAnalyticsTracker.kt`, `app/build.gradle.kts` | Firebase Analytics, Crashlytics e Remote Config estão integrados. Eventos incluem sessão, recurso/tela, versão e propriedades de ambiente; Crashlytics é dependência ativa. |
| Remoto | `CompositeAnalyticsTracker.kt`, `AdminSyncWorker.kt` | Eventos, diagnósticos não contaminados e feedback podem seguir para Worker administrativo Cloudflare, com ID anônimo, modelo, Android, versão e canal. |
| Publicidade | `AndroidManifest.xml`, `ConsentManager.kt` | Google Mobile Ads/AdMob está integrado e condicionado ao UMP aplicável. |

## Limites conhecidos

O código não define prazo único de retenção para Firebase, Google, Cloudflare ou Vercel, nem permite inferir seus papéis contratuais em todos os fluxos. A política declara esse limite e remete à configuração e política de cada fornecedor, sem afirmar que serviços não identificados existem.

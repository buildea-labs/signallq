# Identidade do velocímetro

## Fonte de verdade

O Android e o design system oficial prevalecem. A auditoria foi feita no repositório [buildea-labs/signallq](https://github.com/buildea-labs/signallq) no commit `92fefdd8754f238bd93f1293a32dd34b3a620c6a`: `android/app/src/main/kotlin/io/veloo/app/kotlin/ui/screen/VelocidadeScreen.kt`, `ui/component/GaugeCircular.kt`, `ui/SignallQTheme.kt` e `feature/speedtest/.../MeasurementStatus.kt`.

| Android | Web/PWA | Token e apresentação |
| --- | --- | --- |
| `AGUARDANDO` / `Preparando o teste…` | `idle`, `preparando` | `--accent`; estado textual, sem depender do arco |
| `LATÊNCIA` / `Verificando a resposta do servidor…` | `latencia` | `--phase-latencia` |
| `DOWNLOAD` / `Medindo a velocidade de download…` | `download` | `--phase-download` |
| `UPLOAD` / `Medindo a velocidade de upload…` | `upload` | `--phase-upload` |
| `CONCLUÍDO` / `Quase pronto…` | `processando`, `concluido` | `--success` |
| `Completo` | resultado completo | `--success`; valor compacto e texto explícito |
| `Parcial` | resultado parcial | `--warning`; valor compacto, cópia de limitação e repetição orientada |
| `Inconclusivo` | resultado inconclusivo | `--warning`; valor compacto, amostra insuficiente explícita |
| `Contaminado` | resultado contaminado | `--warning`; valor compacto, mudança de conexão explícita |
| `Cancelado` | cancelamento | `--text-secondary`; sem valor inventado e motivo explícito |
| erro | problemas do motor | `--error`; sem valor inventado e mensagem explícita |

O velocímetro Web segue a hierarquia Android: valor, unidade e fase. Arco e agulha usam a cor semântica; rótulo e narrativa continuam visíveis para que cor e movimento nunca sejam a única indicação. A versão compacta é usada nos desfechos e conserva a mesma linguagem visual. Em tema escuro, os mesmos tokens são redefinidos pelo design system. A região anunciada a tecnologias assistivas contém somente a fase ou o desfecho, não cada atualização de valor.

## Divergência necessária

O Android mostra o velocímetro em uma tela dedicada durante a execução. A Web/PWA o mantém no fluxo da página por restrição de navegação do navegador; semântica, tokens e copies são preservados.

As narrativas de desfecho `Medição concluída.`, as explicações de parcial/inconclusivo/contaminado e `Você cancelou a medição antes do fim.` são adaptações Web para o contexto do resultado inline: o Android fornece os mesmos rótulos de status em `MeasurementStatus.labelPt()`, mas apresenta as explicações em cards e callouts distintos. O erro usa a copy Android `Não foi possível completar o teste.`. A adaptação mantém o significado, evita uma tela sem contexto após o recolhimento e não cria nomes alternativos para métricas ou estados.

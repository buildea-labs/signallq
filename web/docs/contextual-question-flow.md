# Fluxo contextual de perguntas Web

Status: US #8. A fronteira local `ContextualQuestionFlow` está na versão 1 e recebe apenas `MeasurementSessionContext` e respostas declaradas pelo visitante. Não recebe nem tenta inferir telemetria nativa de Wi-Fi, fibra, gateway ou sinal.

## Mapeamento explícito da entrada da US #7

| Problema declarado | Roteiro Web | Referência de copy Android |
| --- | --- | --- |
| `lenta` | quando ocorre; situação se for constante | `internet_lenta` |
| `travando` | mesmo roteiro de lentidão | `internet_lenta` |
| `cai-com-frequencia` | frequência declarada | `nao_sei_q2_cai` |
| `wifi-nao-chega-bem` | área e, se necessário, distância ou momento | `wifi_oscilando` |
| `jogos-ou-chamadas-ruins` | atividade, depois dispositivo ou tipo de chamada | `jogos_travando` e `chamadas_ruins` |

As perguntas são opcionais. Um pulo é registrado como `answerId: null`, isto é, dado indisponível; nunca é convertido em uma resposta presumida. Respostas fora das opções retornam `invalid_answer` sem avançar. O fluxo suporta `concluded` sem perguntas para teste direto, `awaiting_answer`, `invalid_answer`, `insufficient_data` e `unavailable` para incompatibilidade de versão.

`concluded` encerra somente a coleta de contexto e não representa conclusão diagnóstica, causa ou recomendação. A migração Cloudflare deve introduzir um adaptador que preserve os mesmos códigos, versionamento e estados antes de retirar o resolvedor local.

## Retomada local

`measurementSessionStore` versão 1 guarda em `sessionStorage` somente o contexto browser-safe, as respostas declaradas e se o questionário já foi aberto após a medição. Ao recarregar ou navegar na mesma sessão do navegador, a pergunta pendente é resolvida novamente a partir desses dados. Uma nova medição, direta ou por problema, limpa respostas e ativação anteriores. Conteúdo ausente, incompatível, corrompido ou armazenamento bloqueado é tratado como contexto indisponível e nunca impede o teste.

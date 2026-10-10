# Modos de teste: Rápido e Completo

Status: parâmetros de medição do Site/PWA para a US #5. Não são regras de diagnóstico e não alteram o motor Android ou o contrato Cloudflare.

## Propósito e diferença verificável

| Parâmetro | Rápido | Completo | Efeito prático |
| --- | ---: | ---: | --- |
| Solicitações de latência | 15 (até 14 usadas após descartar a primeira) | 25 (até 24 usadas) | O Completo observa mais variação de resposta. |
| Mínimo de respostas válidas | 8 | 18 | O Completo só é completo com mais evidência de latência. |
| Janela de download | 7 s | 18 s | Mais tempo para a taxa se estabilizar. |
| Janela de upload | 7 s | 18 s | Mais tempo para a taxa se estabilizar. |
| Streams máximos, download/upload | 4 / 4 | 8 / 8 | O Completo explora mais paralelismo quando ele melhora a taxa observada. |
| Warm-up excluído | 1 s | 2 s | Evita usar o início transitório no cálculo. |

Rápido é uma triagem de aproximadamente 20 segundos em condições comuns e tem menor consumo porque limita download e upload a sete segundos cada. Completo costuma levar aproximadamente 40 segundos, coleta uma janela de carga 2,57 vezes maior e pode consumir mais dados; o volume real depende da taxa alcançada e do navegador. Nenhum dos dois afirma a velocidade contratada.

## Cálculos e qualidade

Para cada fase de transferência, a taxa instantânea por intervalo é `Mbps = bytes * 8 / (milissegundos / 1000) / 1_000_000`. As amostras anteriores ao warm-up são excluídas. Das restantes, os primeiros 35% são descartados para reduzir o efeito de aceleração; a velocidade exibida é a média das amostras remanescentes e o pico é o maior valor válido.

Na latência, a primeira solicitação é descartada. A latência-base é a mediana das respostas válidas; picos acima de três vezes essa mediana não entram na mediana reportada. Jitter é a média das diferenças absolutas entre respostas aceitas consecutivas. Perda é `timeouts / solicitações após a primeira * 100`.

Uma execução é `complete` apenas quando download e upload terminam na janela prevista e alcançam o mínimo de respostas de latência daquele modo. Com cinco ou mais respostas, mas abaixo do mínimo do modo ou com fase de transferência incompleta, ela é `partial`: as métricas medidas permanecem visíveis, sem conclusão adicional. Abaixo de cinco respostas ela é `inconclusive`. Mudança de rede durante a execução é `contaminated` e exige repetição.

## Limites e contrato oficial

O modo é contexto de qualidade da medição. A API oficial v1 não possui campo semântico para declarar `rapido` ou `completo`; portanto o Site/PWA não envia um atributo não documentado nem deduz uma classificação própria. Quando o contrato Android/Cloudflare expuser esse metadado, ele deverá ser incluído pelo mapeador e avaliado exclusivamente pelo Worker oficial.

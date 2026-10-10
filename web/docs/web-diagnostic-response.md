# Resposta diagnóstica Web local

Status: US #9. `WebDiagnosticResponse` versão 1 organiza a leitura local do navegador em conclusão, impacto, confiança, próxima ação e, somente quando necessário, CTA Android contextual.

Ela usa conjuntamente download, upload, latência, jitter, resposta sob carga, estado da execução e contexto declarado. Download isolado nunca gera um veredito geral. Estados parcial, inconclusivo e contaminado têm confiança baixa e instruem novo teste.

O resultado é uma hipótese browser-safe, não uma causa confirmada. Não lê nem presume sinal/canais Wi-Fi, gateway, dispositivos, rede móvel ou fibra. O CTA Android só surge para o relato `wifi-nao-chega-bem`, com a razão explícita de que sinal, canais e alcance exigem capacidade indisponível no navegador.

O resolvedor é uma fronteira temporária: a futura resposta Cloudflare deve preservar a versão, os campos de hierarquia e a separação entre evidência, hipótese e limitações.

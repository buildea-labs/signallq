# Entrada por problema: fronteira local e limite contratual

Status: US #7.

O Site/PWA apresenta cinco relatos do visitante antes da medição: conexão lenta, travando, quedas frequentes, alcance ruim do Wi-Fi e jogos ou chamadas ruins.

Esses relatos não são diagnóstico, classificação ou conclusão. Eles acompanham a sessão de medição na fronteira local `MeasurementSessionContext`, versão 1, que contém somente `entry` e, quando explicitamente escolhido, `declaredProblem`. Essa fronteira é browser-safe: não contém IP, identificador de dispositivo, localização, SSID ou dados inferidos.

A versão 6 do contrato oficial Android/Cloudflare (`DiagnosticSnapshot`) ainda não define um campo para esses relatos. Portanto, `MeasurementSessionContext` não é anexado ao `POST /api/diagnostic/evaluate` nesta US e não altera Android, Worker ou motor de medição. Ele fica disponível ao próximo motor interno Web que for autorizado a consumi-lo.

Quando o contrato oficial publicar um campo próprio, a adaptação deve ser feita no mapeador de transporte e enviada sem transformar, para que o Worker oficial permaneça a única fonte de perguntas, regras e conclusões.

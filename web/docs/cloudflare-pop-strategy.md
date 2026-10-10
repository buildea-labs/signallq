# Estratégia de borda Cloudflare

Status: decisão técnica da US #6. Aplica-se aos modos Rápido e Completo; não altera o motor de diagnóstico Android nem o contrato da API oficial.

## Decisão

O Site/PWA usa os endpoints únicos do `speed.cloudflare.com` e deixa a rede Cloudflare escolher automaticamente a borda Anycast. Não há seletor de servidor, país ou região: uma seleção manual no navegador não teria efeito real sobre o roteamento e seria uma promessa falsa.

Cloudflare documenta que a sua rede Anycast anuncia os mesmos endereços a partir de vários data centers e encaminha cada requisição para uma borda próxima e disponível. A própria documentação também ressalva que isso não significa necessariamente o data center geograficamente mais próximo: peering, rota e capacidade podem determinar uma alternativa melhor. Portanto, o Site/PWA não usa GPS nem tenta escolher uma região.

## PoP efetivamente observado

Quando as respostas de medição disponibilizam `cf-meta-colo` para o navegador via CORS e todas indicam o mesmo PoP, o cliente aceita apenas um código IATA de três letras e mostra `Borda Cloudflare · PoP XXX` no resultado. Esse código descreve a borda que respondeu àquela execução, não uma localização do usuário. Se o header não estiver disponível ou as respostas divergirem, o fallback é `Borda Cloudflare (roteamento automático)`; o produto não infere ou inventa um PoP.

O cliente não lê nem armazena IP, país, cidade, CEP, latitude, longitude ou fuso dos headers de medição.

## Evidência da validação em 2026-08-01

Uma requisição real a `https://speed.cloudflare.com/__down?bytes=1` retornou `200`, `Server: cloudflare`, `CF-RAY: …-GIG` e `colo: GIG` neste ambiente. Isso confirma uma borda real para a execução observada, mas não autoriza fixá-la para outros usuários.

As fontes oficiais consultadas:

- [Cloudflare IP addresses: rede Anycast](https://developers.cloudflare.com/fundamentals/concepts/cloudflare-ip-addresses/)
- [Cloudflare HTTP headers: Cf-Ray e data center](https://developers.cloudflare.com/fundamentals/reference/http-headers/)
- [Cloudflare: verificação do colo via `Cf-Ray` ou `/cdn-cgi/trace`](https://developers.cloudflare.com/data-localization/how-to/)

## Limites

`Cf-Ray` é uma evidência útil de borda, mas não é usado pela UI quando não é legível pelo navegador devido a CORS. Alterações futuras nos endpoints, headers expostos ou arquitetura da Cloudflare exigem nova validação antes de mudar esta estratégia.

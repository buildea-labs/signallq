# Auditoria institucional — US #26

Data: 1º de agosto de 2026. Escopo: `/quem-somos`, `/como-medimos`,
`/privacidade`, `/termos` e a navegação institucional compartilhada.

## Evidências verificadas

- Cada rota institucional agora declara `title`, `description`, canonical,
  Open Graph, Twitter Card e `robots` pelo Metadata API do Next.js. Portanto,
  esses elementos existem no HTML inicial, sem depender de JavaScript.
- O sitemap lista as quatro rotas e o `robots.txt` aponta para ele. Os
  fragmentos `#android` e `#web-pwa` de `/privacidade` não são rotas nem URLs
  indexáveis separadas; o canonical permanece `/privacidade`.
- A política deixa conteúdo comum visível uma vez, usa botões nativos com
  estado pressionado para a seleção de plataforma e oferece as duas versões no
  fallback `noscript`. Os accordions usam `details` e `summary` nativos.
- A navegação mobile deixou de declarar o padrão ARIA `menu`, que exigiria
  navegação por setas. Ela é uma navegação comum, com links tabuláveis; Escape
  continua fechando o painel.
- As variações claro/escuro repetidas do logotipo são decorativas. O link da
  marca recebeu nome acessível único e os demais logotipos foram removidos da
  árvore acessível para não duplicar anúncios de leitor de tela.
- Ilustrações institucionais são SVGs inline, sem download adicional, e são
  decorativas quando não recebem rótulo. O CSS global reduz animações quando
  `prefers-reduced-motion: reduce` está ativo.

## Limites desta auditoria

Foram executados os gates automatizados registrados na PR. Esta auditoria não
alega uma medição em dispositivo físico, Lighthouse remoto, Core Web Vitals de
produção ou validação por leitor de tela humano; esses itens dependem de uma
sessão de navegador/dispositivo e de ambiente publicado.

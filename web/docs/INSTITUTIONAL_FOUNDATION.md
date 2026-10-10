# Fundação institucional

Os componentes em `src/components/institutional/InstitutionalFoundation.tsx` são a moldura reutilizável das páginas institucionais. Eles usam somente tokens definidos em `src/styles/tokens.css`, funcionam em temas claro e escuro e não introduzem conteúdo ou ilustrações obrigatórias.

- `InstitutionalHero`: cabeçalho com overline, título, resumo, metadados e ilustração opcional.
- `ReadingLayout`: limita a medida de leitura a 720px.
- `HighlightSection`: destaca uma informação sem transformar a página em uma coleção de cards.
- `StepsBlock`: lista ordenada para etapas.
- `InformationGroup`: pares `dt`/`dd` para informações factuais.
- `AccessibleAccordion`: usa `details`/`summary` nativos; funciona por teclado e expõe o estado expandido sem JavaScript.
- `InstitutionalCta`: chamada de ação coerente com os botões do produto.
- `IllustrationWrapper`: recebe SVG ou outra arte leve. Sem `alt`, a arte é decorativa; com `alt`, o texto fica disponível para leitores de tela.

`DocPage` usa `InstitutionalHero`, `ReadingLayout` e `InstitutionalCta` sem alterar as cópias nem converter as seções existentes em outro formato. Os demais componentes ficam disponíveis para a composição pontual das páginas nas USs posteriores.

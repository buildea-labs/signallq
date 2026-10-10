# SignallQ Web

## Identidade e estado atual

- **Organização:** `buildea-labs`
- **Finalidade:** landing page pública do SignallQ Android — divulgação do app e direcionamento para a Play Store, com as páginas legais exigidas pelo Google Play.
- **Classificação:** produto.
- **Estado atual:** aplicação Next.js com App Router, sem PWA, três rotas públicas (`/`, `/privacidade`, `/termos`) e um Route Handler de telemetria (`/api/track`), com validações Vitest.
- **Mudança de escopo (2026-08-27):** o site deixou de ser o produto de medição/PWA e passou a ser só a landing do app Android. Ferramentas de diagnóstico, comparador de planos, PWA/Serwist, AdSense e conteúdo editorial/SEO foram removidos do repositório; ver histórico de commits para o corte completo.
- **Publicação (2026-08-28):** PR #160 mergeada em `main` e publicada em produção via workflow **Deploy manual na Vercel** (`target=production`). `https://signallq.com` já serve a landing reduzida.

## Escopo e exclusões

- **Pertence ao repositório:** landing pública do app Android (`/`), Política de Privacidade e Termos de Uso (`/privacidade`, `/termos`), o CTA de download para a Play Store e o proxy de telemetria do clique nesse CTA.
- **Não pertence:** aplicativo Android e Workers do repositório `signallq`, portal administrativo `buildea-admin`, políticas corporativas completas, projetos pessoais, e (desde a mudança de escopo) qualquer medição de conexão, histórico local, PWA ou comparador de planos — essas funcionalidades foram descontinuadas neste repositório, não movidas para outro lugar dentro dele.

## Arquitetura comprovada

- **Componentes principais:** Next.js 16, React 19, TypeScript, Tailwind CSS, `src/app/`, `src/components/`, `src/lib/`, `src/styles/` e `public/`.
- **Rotas:** `src/app/page.tsx` (landing), `src/app/privacidade/`, `src/app/termos/`, `src/app/api/track/route.ts` (Route Handler); `src/middleware.ts` restringe o allowlist de rotas públicas a essas três páginas.
- **Integrações:** link de download para a Play Store configurável via `NEXT_PUBLIC_SIGNALLQ_PLAY_STORE_URL` (`src/lib/config.ts`), proxy server-side de telemetria (`/api/track`) que encaminha ao Worker administrativo do SignallQ. Disponibilidade de serviços externos é a validar.
- **Dependências:** declaradas em `package.json`.

## Comandos essenciais comprovados

- **Instalação:** `npm ci`.
- **Execução:** `npm run dev`.
- **Lint:** `npm run lint`.
- **Typecheck:** `npm run typecheck`.
- **Testes:** `npm test`.
- **Build:** `npm run build`.
- **Validações específicas:** a CI executa instalação, lint, typecheck, testes e build. Alterações de acessibilidade, SEO técnico ou interface exigem as validações locais aplicáveis em `skills/`.

## Restrições

- **Acessibilidade e performance:** preservar semântica, navegação por teclado, responsividade e desempenho; mudanças de UI devem manter tokens e componentes existentes quando aplicável.
- **Privacidade:** segredos, incluindo `SITE_INGEST_KEY`, devem permanecer somente no servidor.
- **SEO técnico:** Renan responde por rotas, metadados, redirecionamentos, indexação e dados estruturados. SEO editorial e aquisição pertencem a Marcos.
- **Custos:** mudanças em telemetria, hospedagem ou serviços externos exigem aprovação do Luiz quando criarem custo ou compromisso externo.
- **Publicação:** deploy, produção, alteração pública de marca, rotas públicas, consentimento ou mudança irreversível exigem aprovação explícita do Luiz.

## Agentes aplicáveis

Reavaliado em 2026-08-28 após a redução de escopo: com o site limitado a três rotas estáticas e um proxy de telemetria de clique, nem todo agente listado no portfólio tem trabalho rotineiro aqui. A lista abaixo distingue quem é central deste repositório de quem atua só sob demanda.

- **Líder funcional (central):** Claudete — prioridade, escopo e critérios de aceite da landing.
- **Responsável técnico web (central):** Renan — implementação, SEO técnico, acessibilidade, performance e o único dono técnico do repositório.
- **Revisão independente (central):** Caio; não implementa a entrega que revisa — obrigatório em qualquer mudança de código, segurança ou produção.
- **Design (sob demanda):** Juliana — só quando a landing, o CTA ou as páginas legais mudarem visualmente; não há mais fluxo de produto complexo a desenhar.
- **Growth e SEO editorial (sob demanda, papel residual):** Marcos — o repositório não tem mais conteúdo editorial, blog ou comparador; acionar apenas para mensagem/posicionamento do CTA de download, não como rotina.
- **Operações, métricas e dados (sob demanda, papel residual):** Gustavo — o único dado é o evento de clique em `/api/track`; acionar apenas se o catálogo dessa métrica ou o pipeline de telemetria mudar, não há mais superfície de dados a manter.
- **Fonte organizacional:** os agentes corporativos canônicos vivem em `../ai-governance/agents/`. Este arquivo não redefine escopo organizacional, só a prioridade de engajamento dentro deste repositório.
- **Skills locais:** `skills/` contém instruções específicas deste repositório.

## Critérios locais de conclusão

- O escopo autorizado foi atendido, os comandos e validações aplicáveis têm evidência, acessibilidade/SEO técnico foram avaliados quando afetados e Caio revisou mudanças com código, segurança, produção ou risco relevante.

## Fontes complementares

- `README.md`
- `package.json`
- `next.config.ts`
- `.env.example`
- `docs/deploy-vercel.md`
- `skills/quality-gates/SKILL.md`
- `skills/accessibility-seo-review/SKILL.md`
- `skills/architecture-guardrails/SKILL.md`
- `../ai-governance/policies/agent-operating-contract.md`
- `../ai-governance/policies/demand-routing.md`

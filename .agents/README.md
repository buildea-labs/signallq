# Agentes e skills do SignallQ

A governança vive em [`AGENTS.md`](../AGENTS.md). Este diretório contém procedimentos e artefatos de trabalho; os perfis nativos vivem em [`.codex/agents/`](../.codex/agents/) (Codex) e [`.claude/agents/`](../.claude/agents/) (Claude Code).

## Equipe

- **Claudete** — Product Lead e persona principal da sessão.
- **Rian** — Android Engineer.
- **Marcelo** — Diagnostic Systems Engineer.
- **Tiago** — QA & Reliability.
- **Camillo** — Principal Engineer / System Architect transversal.

O orquestrador principal (Codex ou Claude Code) integra o trabalho. Não simule conversa entre personagens nem declare revisão que não ocorreu.

## Estrutura

```text
AGENTS.md                  governança do produto
.codex/agents/*.toml       especialistas delegáveis
.agents/WORKFLOW.md        fluxo operacional
.agents/architecture-plan.md plano sistêmico quando necessário
.claude/skills/            skills canônicas
.agents/skills/            espelho de compatibilidade Codex
.github/skills/            espelho de compatibilidade GitHub
```

## Skills

Skills são procedimentos. O nome do agente responsável não deve ser embutido como regra da skill; o roteamento vem do `AGENTS.md`.

### Produto e experiência

- `SignallQ-design` — referência/produção visual.
- `design-check` — checagem pontual contra Design System.
- `auditar-ux` — auditoria ampla de usabilidade e acessibilidade.
- `growth-check` — checklist de loja/ASO/superfície pública.
- `estimativa-impacto` — tamanho, risco e gatilhos de arquitetura.
- `analytics-spec` — especificação de telemetria.

### Android

- `regras-android` — APIs, permissões e particularidades Android.
- `padroes-compose` — Compose.
- `protocolo-ci-android` — pipeline Android.
- `protocolo-ktlint` — lint/formatação.

### Diagnóstico

- `motor-diagnostico` — arquitetura funcional do diagnóstico/speedtest.
- `regras-diagnostico-rede` — thresholds e regras técnicas.
- `reconhecimento-equipamento-rede` — equipamentos e identificação.

### Engenharia e operação

- `inventario` — localizar/reutilizar implementação existente.
- `verificar-modulo` — fronteiras e dependências de módulos.
- `cloudflare-d1-console` — operação D1 quando aplicável.
- `handoff` — passagem formal de contexto quando realmente necessária.
- `check-done` — evidência de conclusão.
- `checar-release` — release readiness.
- `gerar-docs` — documentação viva.
- `impeccable` — tooling de qualidade visual.

## Fonte canônica e espelhos

`.claude/skills/` é a fonte canônica; `.agents/skills/` e `.github/skills/` são espelhos.

Depois de editar skill, execute:

```bash
./scripts/sync-skills-mirrors.sh
./scripts/sync-skills-mirrors.sh --check
```

Os espelhos não devem ganhar regras próprias.

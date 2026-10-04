# Contribuindo com o SignallQ

Este projeto usa agentes (Codex e Claude Code coexistem como orquestradores) como squad de desenvolvimento. As
regras completas de processo vivem em [`AGENTS.md`](AGENTS.md); papéis, autonomia e escopo de
cada agente vivem em `.claude/agents/*.md` — este arquivo existe só para dar visibilidade rápida.

## Checks obrigatórios para merge em `main`

- `Ktlint`, `Detekt` e `Unit Tests` (GitHub Actions) — branch protection exige os três verdes.
- A branch não precisa estar byte-a-byte atualizada com `main` (`strict: false` em `required_status_checks`, conferido via API em 2026-10-04); o workflow `auto-update-branch.yml` atualiza PRs abertas a cada push em `main`.

## Antes de abrir PR

- Rode `./scripts/setup-hooks.sh` (ou `.ps1` no Windows) uma vez por clone — ativa os hooks
  versionados em `scripts/hooks/`: guardrail cross-stack no commit e lint local no push.
- Use o template de PR (`.github/pull_request_template.md`).
- Nunca declare "PR mergeada"/"teste passou"/"publicado em produção" sem verificar de fato
  (`gh pr view`, `gh pr checks`, endpoint real) — ver
  `../ai-governance/policies/agent-operating-contract.md`.

Para tudo além disso (papéis dos agentes, autonomia, rotinas, disciplina de branches) ver
[`AGENTS.md`](AGENTS.md) e `.claude/agents/*.md`.

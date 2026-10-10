// Funções puras do resultado do teste de velocidade — sem React, sem rede.
// Mantidas separadas do hook de medição (useSpeedTestEngine.ts) pra poder
// testar o veredito com entradas determinísticas, sem mock de fetch.

export type UsageLevel = { n: string; s: string; c: string }

const GREEN = '#146C2E'
const AMBER = '#8A5000'
const RED = '#BA1A1A'

export function formatMetric(value: number | null): string {
  if (value == null) return '—'
  return value >= 100 ? String(Math.round(value)) : value.toFixed(1)
}

export function getVerdict(downloadMbps: number | null): { title: string; subtitle: string } {
  const d = downloadMbps ?? 0
  if (d >= 100) return { title: 'Sua internet está ótima.', subtitle: 'Rápida o bastante para vários aparelhos ao mesmo tempo.' }
  if (d >= 25) return { title: 'Sua internet está boa.', subtitle: 'Atende bem o dia a dia: vídeo, trabalho e redes sociais.' }
  if (d >= 8) return { title: 'Sua internet dá para o básico.', subtitle: 'Funciona para o essencial, mas pode ficar apertada com vários aparelhos.' }
  return { title: 'Internet lenta.', subtitle: 'Dá para mensagens e páginas simples. Vídeos e chamadas podem travar.' }
}

function level(ok: boolean, mid: boolean): { s: string; c: string } {
  if (ok) return { s: 'Tranquilo', c: GREEN }
  if (mid) return { s: 'Com limites', c: AMBER }
  return { s: 'Vai travar', c: RED }
}

export function getUsageLevels(downloadMbps: number | null, uploadMbps: number | null, pingMs: number | null): UsageLevel[] {
  const d = downloadMbps ?? 0
  const u = uploadMbps ?? 0
  const p = pingMs ?? 0
  return [
    { n: 'Vídeo em HD', ...level(d >= 8, d >= 4) },
    { n: 'Vídeo em 4K', ...level(d >= 30, d >= 15) },
    { n: 'Videochamada', ...level(u >= 3 && p < 100, u >= 1.5) },
    { n: 'Jogos online', ...level(p < 50 && d >= 10, p < 100) },
  ]
}

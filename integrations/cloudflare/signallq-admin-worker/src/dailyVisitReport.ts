// Informe diário de visitas do signallq.com no Discord. Mesmo formato do relatório do Lagcheck
// (embed com D-1 + acumulado), mas com os números do SignallQ e enviado por este Worker, no
// mesmo canal/webhook. Os dados vêm de analytics_events (platform='web'), já alimentada pelo
// /api/track do site — não há contador paralelo em KV.
import type { Env } from './index.ts'

export const FEATURE_DOWNLOAD_CLICADO = 'download_app_clicado'
export const FEATURE_TESTE_INICIADO = 'teste_velocidade_iniciado'

export interface WebStats {
  visits: number
  downloads: number
  tests: number
}

const BRT_OFFSET = '-03:00'

/** Data (YYYY-MM-DD, America/Sao_Paulo) do dia anterior a `now`. */
export function previousDayBrt(now: Date): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Sao_Paulo' }).format(new Date(now.getTime() - 24 * 60 * 60 * 1000))
}

/** Janela [início, fim) em epoch segundos do dia BRT informado. */
export function dayWindowBrt(date: string): { from: number; to: number } {
  const from = Math.floor(Date.parse(`${date}T00:00:00${BRT_OFFSET}`) / 1000)
  return { from, to: from + 24 * 60 * 60 }
}

async function count(db: D1Database, sql: string, binds: unknown[]): Promise<number> {
  const row = await db.prepare(sql).bind(...binds).first<{ n: number }>()
  return Number(row?.n ?? 0)
}

export async function collectWebStats(db: D1Database, window?: { from: number; to: number }): Promise<WebStats> {
  // O site grava created_at em milissegundos (Date.now()) e o app em segundos; a janela do dia
  // precisa cobrir as duas escalas, senão o "Ontem" sai zerado.
  const range = window
    ? ' AND ((created_at >= ? AND created_at < ?) OR (created_at >= ? AND created_at < ?))'
    : ''
  const range_binds = window ? [window.from, window.to, window.from * 1000, window.to * 1000] : []
  const base = `FROM analytics_events WHERE platform = 'web'${range}`
  const [visits, downloads, tests] = await Promise.all([
    count(db, `SELECT COUNT(DISTINCT session_id) AS n ${base} AND event_name = 'session_start'`, range_binds),
    count(db, `SELECT COUNT(*) AS n ${base} AND event_name = 'feature_used' AND feature_id = ?`, [...range_binds, FEATURE_DOWNLOAD_CLICADO]),
    count(db, `SELECT COUNT(*) AS n ${base} AND event_name = 'feature_used' AND feature_id = ?`, [...range_binds, FEATURE_TESTE_INICIADO]),
  ])
  return { visits, downloads, tests }
}

function pct(part: number, total: number): string {
  return total > 0 ? ((part / total) * 100).toFixed(1) : '0.0'
}

function lines(s: WebStats, visitsLabel: string, downloadsLabel: string, testsLabel: string, rateLabel: string): string {
  return [
    `• 👁️ **${visitsLabel}:** ${s.visits}`,
    `• 📲 **${downloadsLabel}:** ${s.downloads}`,
    `• ⚡ **${testsLabel}:** ${s.tests}`,
    `• 📈 **${rateLabel}:** **${pct(s.downloads, s.visits)}%**`,
  ].join('\n')
}

export function buildVisitReportPayload(date: string, daily: WebStats, total: WebStats, now: Date) {
  return {
    username: 'SignallQ Analytics',
    embeds: [
      {
        title: '📊 Relatório Diário do Site (D-1)',
        description: 'Acessos ao **signallq.com** e conversão para download do app.',
        color: 0x5b21d6,
        fields: [
          { name: `📅 Ontem (D-1: ${date})`, value: lines(daily, 'Visitas', 'Cliques em Baixar', 'Testes de velocidade', 'Taxa de Conversão'), inline: false },
          { name: '🌐 Acumulado Geral', value: lines(total, 'Visitas Totais', 'Cliques Totais', 'Testes Totais', 'Conversão Geral'), inline: false },
        ],
        footer: { text: 'SignallQ • Relatório Diário Automático (09:00 BRT)' },
        timestamp: now.toISOString(),
      },
    ],
  }
}

export async function sendDailyVisitReport(env: Env, now: Date = new Date(), fetchImpl: typeof fetch = fetch): Promise<void> {
  if (!env.DISCORD_WEBHOOK_URL) return
  const date = previousDayBrt(now)
  const [daily, total] = await Promise.all([collectWebStats(env.DB, dayWindowBrt(date)), collectWebStats(env.DB)])
  const res = await fetchImpl(env.DISCORD_WEBHOOK_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(buildVisitReportPayload(date, daily, total, now)),
  })
  if (!res.ok) throw new Error(`Discord webhook respondeu ${res.status}`)
}

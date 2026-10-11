import assert from 'node:assert/strict'
import test from 'node:test'
import { buildVisitReportPayload, dayWindowBrt, previousDayBrt, sendDailyVisitReport } from '../src/dailyVisitReport.ts'
import { buildEnv } from './support.ts'

// DB falso: devolve o n por trecho de SQL e guarda os binds para conferir a janela.
class StatsDb {
  readonly calls: Array<{ sql: string; binds: unknown[] }> = []
  prepare(sql: string) {
    const call = { sql, binds: [] as unknown[] }
    this.calls.push(call)
    return {
      bind: (...binds: unknown[]) => {
        call.binds = binds
        return { first: async () => ({ n: sql.includes('DISTINCT') ? 100 : sql.includes('?') && String(binds.at(-1)) === 'download_app_clicado' ? 7 : 3 }) }
      },
    }
  }
}

test('previousDayBrt usa o dia anterior em America/Sao_Paulo', () => {
  assert.equal(previousDayBrt(new Date('2026-10-11T12:00:00Z')), '2026-10-10')
  // 01:00 UTC ainda é o dia anterior em BRT
  assert.equal(previousDayBrt(new Date('2026-10-11T01:00:00Z')), '2026-10-09')
})

test('dayWindowBrt cobre 24h a partir de 00:00 BRT (03:00 UTC)', () => {
  const { from, to } = dayWindowBrt('2026-10-10')
  assert.equal(new Date(from * 1000).toISOString(), '2026-10-10T03:00:00.000Z')
  assert.equal(to - from, 86400)
})

test('payload separa D-1 e acumulado e calcula a conversão', () => {
  const p = buildVisitReportPayload('2026-10-10', { visits: 200, downloads: 10, tests: 40 }, { visits: 1000, downloads: 25, tests: 90 }, new Date('2026-10-11T12:00:00Z'))
  const [daily, total] = p.embeds[0].fields
  assert.match(daily.value, /Visitas:\*\* 200/)
  assert.match(daily.value, /5\.0%/)
  assert.match(total.value, /2\.5%/)
})

test('sem DISCORD_WEBHOOK_URL não envia nada', async () => {
  let called = false
  await sendDailyVisitReport(buildEnv(new StatsDb() as never, { DISCORD_WEBHOOK_URL: undefined }), new Date(), (async () => { called = true; return new Response() }) as typeof fetch)
  assert.equal(called, false)
})

test('envia o embed ao webhook e falha se o Discord recusar', async () => {
  const db = new StatsDb()
  const sent: Array<{ url: string; body: any }> = []
  const ok = (async (url: string, init: RequestInit) => { sent.push({ url, body: JSON.parse(String(init.body)) }); return new Response(null, { status: 204 }) }) as unknown as typeof fetch
  const env = buildEnv(db as never, { DISCORD_WEBHOOK_URL: 'https://discord.test/hook' })
  await sendDailyVisitReport(env, new Date('2026-10-11T12:00:00Z'), ok)
  assert.equal(sent.length, 1)
  assert.equal(sent[0].url, 'https://discord.test/hook')
  assert.equal(sent[0].body.username, 'SignallQ Analytics')
  // todas as consultas filtram platform = 'web'
  assert.ok(db.calls.every((c) => c.sql.includes("platform = 'web'")))

  const bad = (async () => new Response(null, { status: 500 })) as unknown as typeof fetch
  await assert.rejects(sendDailyVisitReport(env, new Date('2026-10-11T12:00:00Z'), bad), /500/)
})

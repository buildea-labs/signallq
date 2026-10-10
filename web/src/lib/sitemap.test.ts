import { readFileSync } from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'
import { PAGE_META } from './pageMetaCatalog'
import { SITE_ORIGIN } from './routeMetadata'

// Valida que public/sitemap.xml cobre todas as rotas indexáveis do catálogo
// único de metadados — evita gaps como o de /privacidade/matriz (existia no
// catálogo mas nunca tinha sido adicionada ao sitemap).
const REPO_ROOT = path.resolve(__dirname, '..', '..')

function loadSitemapLocs(): string[] {
  const raw = readFileSync(path.join(REPO_ROOT, 'public', 'sitemap.xml'), 'utf-8')
  const matches = [...raw.matchAll(/<loc>(.*?)<\/loc>/g)]
  return matches.map((match) => match[1])
}

function indexableCatalogPaths(): string[] {
  const paths = new Set<string>()
  for (const meta of Object.values(PAGE_META)) {
    if (meta.robots?.includes('noindex')) continue
    paths.add(meta.path)
  }
  return [...paths]
}

describe('public/sitemap.xml', () => {
  it('contains an entry for every indexable route in the page meta catalog', () => {
    const locs = loadSitemapLocs()
    const missing = indexableCatalogPaths().filter((routePath) => !locs.includes(`${SITE_ORIGIN}${routePath}`))
    expect(missing).toEqual([])
  })
})

// Gera os ícones PWA ("any" e "maskable", 192x192 e 512x512) a partir do
// asset oficial da marca já existente em `public/assets/`, sem redesenhar
// nem inventar nada — só recorte/escala/composição (issue #21, decisão de
// Juliana em `implementation_plan.md`/comentário de #21).
//
// Asset-base: `signallq-icon-512-play-store-dark.png` (1024x1024 real,
// apesar do nome dizer "512"). Fundo escuro coerente com
// `background_color`/`theme_color` (#131217) do manifest.json.
//
// Ícones "any" (192/512): reduzem o asset-base sem padding extra — a marca
// já tem margem própria adequada pra um ícone quadrado comum.
//
// Ícones "maskable" (192/512): a marca (ignorando o fundo sólido) precisa
// caber num círculo centralizado de ~66% do diâmetro do canvas (margem
// efetiva ~17%/lado — mais folga que o mínimo W3C de ~10%, porque o arco
// externo e o "cabo" da lupa se estendem na diagonal até perto da borda no
// asset original e são cortados primeiro por uma máscara circular/squircle).
// MASKABLE_SCALE foi calculado medindo a distância radial máxima (a partir
// do centro do canvas) de qualquer pixel que difere da cor de fundo no
// asset-base — ver comentário abaixo — com uma pequena margem de segurança.
//
// Rodar manualmente se o asset-base da marca mudar. Não faz parte do
// `npm run build` (ícones são conteúdo estático versionado).
//
// Uso: node scripts/generate-pwa-icons.mjs
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import sharp from 'sharp'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const ROOT = path.resolve(__dirname, '..')
const ASSETS_DIR = path.join(ROOT, 'public', 'assets')
const SOURCE = path.join(ASSETS_DIR, 'signallq-icon-512-play-store-dark.png')

// #131217 — mesma cor de `background_color`/`theme_color` do manifest.json,
// e é literalmente a cor de fundo já presente no asset-base (confirmado
// amostrando o pixel (0,0) do PNG original: rgb(19,18,23) = 0x131217).
const BACKGROUND = { r: 19, g: 18, b: 23 }

// Calculado a partir do asset-base: distância radial máxima (do centro do
// canvas 1024x1024) de qualquer pixel que difere da cor de fundo em mais de
// 12 (distância euclidiana em RGB) é ~451.81px. Alvo: caber num círculo de
// raio 0.33 * 1024 = 337.92px (diâmetro ~66%). Fator exato = 337.92/451.81
// ≈ 0.748; usamos 0.74 como margem de segurança extra (resulta em diâmetro
// final de ~65.3%, confirmado por teste em `src/lib/manifestIcons.test.ts`
// indiretamente via ausência de transparência e dimensões — a checagem de
// safe-zone em si foi validada manualmente durante a geração).
const MASKABLE_SCALE = 0.74

async function generateAny(size, outputName) {
  const outputPath = path.join(ASSETS_DIR, outputName)
  await sharp(SOURCE).resize(size, size, { fit: 'cover' }).png().toFile(outputPath)
  console.log(`gerado ${outputName} (${size}x${size}, purpose "any")`)
}

async function generateMaskable(size, outputName) {
  const innerSize = Math.round(size * MASKABLE_SCALE)
  const offset = Math.round((size - innerSize) / 2)

  const resizedInner = await sharp(SOURCE).resize(innerSize, innerSize, { fit: 'cover' }).toBuffer()

  const outputPath = path.join(ASSETS_DIR, outputName)
  await sharp({
    create: {
      width: size,
      height: size,
      channels: 4,
      background: { ...BACKGROUND, alpha: 1 },
    },
  })
    .composite([{ input: resizedInner, left: offset, top: offset }])
    .png()
    .toFile(outputPath)
  console.log(`gerado ${outputName} (${size}x${size}, purpose "maskable", marca em ${innerSize}px centralizada)`)
}

async function main() {
  await generateAny(192, 'signallq-icon-192-any.png')
  await generateAny(512, 'signallq-icon-512-any.png')
  await generateMaskable(192, 'signallq-icon-192-maskable.png')
  await generateMaskable(512, 'signallq-icon-512-maskable.png')
}

main().catch((err) => {
  console.error(err)
  process.exitCode = 1
})

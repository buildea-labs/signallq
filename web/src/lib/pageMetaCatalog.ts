// Catálogo único de metadados por rota — consumido tanto pelo cliente
// (useDocumentMeta, upsert pós-hidratação) quanto por `functions/_middleware.ts`
// (injeção no HTML inicial via HTMLRewriter, issue #1369 Fase 2). Uma só fonte
// de verdade: mudar o title/description de uma rota aqui já reflete nos dois.
import type { PageMeta } from './seo'

export const PAGE_META: Record<string, PageMeta> = {
  '/': {
    title: 'SignallQ: descubra por que sua internet está ruim',
    description: 'App Android que analisa sua rede, mostra onde está o problema e diz o que fazer. Grátis, sem cadastro.',
    path: '/',
  },
  '/privacidade': {
    title: 'Política de Privacidade — SignallQ',
    description: 'Política de privacidade do SignallQ: dados tratados pelo app Android, armazenamento e seus direitos.',
    path: '/privacidade',
  },
  '/termos': {
    title: 'Termos de Uso — SignallQ',
    description: 'Termos de uso do aplicativo SignallQ.',
    path: '/termos',
  },
  '/como-funciona': {
    title: 'Como funciona o SignallQ | Do sintoma à solução',
    description: 'Entender, diagnosticar, resolver e confirmar: veja como o SignallQ descobre a causa da internet ruim.',
    path: '/como-funciona',
  },
  '/duvidas': {
    title: 'Dúvidas frequentes | SignallQ',
    description: 'Respostas sobre o app, o diagnóstico com IA, permissões, preço e privacidade.',
    path: '/duvidas',
  },
  '/teste-de-velocidade': {
    title: 'Teste de velocidade da internet | SignallQ',
    description: 'Meça download, upload e latência em 20 segundos e veja o resultado em palavras simples.',
    path: '/teste-de-velocidade',
  },
  '/guias': {
    title: 'Guias de internet e Wi-Fi | SignallQ',
    description: 'Guias simples para entender e resolver problemas de internet lenta, Wi-Fi fraco, latência e velocidade.',
    path: '/guias',
  },
  '/guias/por-que-minha-internet-esta-lenta': {
    title: 'Por que minha internet está lenta? Causas e o que fazer | SignallQ',
    description: 'Wi-Fi, roteador, operadora ou DNS: aprenda a descobrir a causa da internet lenta e como resolver.',
    path: '/guias/por-que-minha-internet-esta-lenta',
  },
  '/guias/como-melhorar-sinal-wifi': {
    title: 'Como melhorar o sinal do Wi-Fi em casa | SignallQ',
    description: 'Posição do roteador, banda 2,4 e 5 GHz, canal e repetidor: ajustes que melhoram o Wi-Fi.',
    path: '/guias/como-melhorar-sinal-wifi',
  },
  '/guias/latencia-jitter-dns': {
    title: 'O que são latência, jitter e DNS | SignallQ',
    description: 'Entenda o que significam ping, jitter, perda de pacotes e DNS e por que importam além da velocidade.',
    path: '/guias/latencia-jitter-dns',
  },
  '/guias/qual-velocidade-de-internet-preciso': {
    title: 'Qual velocidade de internet eu preciso? | SignallQ',
    description: 'Referência de Mbps para vídeo, 4K, videochamada e jogos, e como somar os aparelhos da casa.',
    path: '/guias/qual-velocidade-de-internet-preciso',
  },
}

export const NOT_FOUND_META: PageMeta = {
  title: 'Página não encontrada — SignallQ',
  description: 'A página que você acessou não existe ou foi movida.',
  path: '/404',
  robots: 'noindex,follow',
}

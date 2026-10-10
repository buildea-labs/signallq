import { useEffect } from 'react'

const REVEAL_SELECTOR = '.sq-app-reveal'
const REVEALED_CLASS = 'sq-in'

// Revela seções marcadas com `.sq-app-reveal` conforme entram na viewport.
//
// Historicamente isto era feito com scroll/wheel/resize listeners + um
// setInterval(250ms) verificando `getBoundingClientRect()` manualmente, com
// deduplicação via `el.dataset.sqRev` gravado no DOM real. Isso causava uma
// race condition real (achado 03/08/2026, reproduzida via Playwright com
// scroll rápido/pulo direto para o fim da página, 2 execuções idênticas
// dando resultados diferentes): em StrictMode/dev (e em remontagens de
// hidratação), o efeito roda, monta um `scan()` que já marca elementos fora
// da viewport com `dataset.sqRev = '1'`, e é desmontado antes do próximo
// `check()` revelar esses elementos. Como a segunda montagem real do efeito
// também consulta o mesmo `dataset.sqRev` no DOM (que sobrevive à
// desmontagem), ela pula esses elementos achando que já estão "sendo
// vigiados" por uma instância que na verdade já foi destruída — e eles
// nunca mais recebem `.sq-in`, ficando presos em opacity:0 para sempre.
//
// Correção: usar IntersectionObserver como mecanismo principal (padrão
// nativo do browser para "entrou na viewport") e nunca usar estado
// persistido no DOM (dataset) como dedup entre montagens — cada montagem do
// efeito cria seu próprio observer e decide o que ainda falta revelar só a
// partir do `classList` atual (revelado ou não) e de um `Set` que vive
// somente no closure desta montagem, nunca no DOM. Isso resolve por
// completo a causa raiz da race condition entre montagens.
//
// Isso por si só não basta para "pulos" instantâneos de scroll (tecla End,
// arrastar a scrollbar direto pro fim, `scrollIntoView({behavior:'auto'})`,
// um flick rápido que o browser resolve sem gerar frames intermediários
// visíveis): o IntersectionObserver só notifica quando o estado de
// interseção muda, e um elemento que salta de "abaixo da viewport" direto
// para "acima da viewport" pode nunca ser reportado como interseccionando.
// Por isso, além do observer, um listener leve de `scroll`/`resize`
// re-verifica (via `getBoundingClientRect`, sem `querySelectorAll`, só nos
// elementos ainda pendentes) se a posição final do scroll já ultrapassou o
// elemento — cobrindo exatamente esse caso sem voltar a depender de
// polling constante (`setInterval`).
export function useRevealOnScroll() {
  useEffect(() => {
    let delayIndex = 0
    let disposed = false

    // Navegador/ambiente sem suporte a IntersectionObserver (ex.: jsdom em
    // testes, ou um browser muito antigo): degrada para "revelado desde já"
    // em vez de deixar as seções presas em opacity:0 para sempre — o efeito
    // visual de revelação é puramente progressivo, não deve ser um
    // bloqueador de conteúdo.
    if (typeof IntersectionObserver === 'undefined') {
      document.querySelectorAll<HTMLElement>(REVEAL_SELECTOR).forEach((el) => {
        el.classList.add(REVEALED_CLASS)
      })
      return
    }

    // Elementos que esta instância do efeito está vigiando e ainda não
    // revelou. Vive só neste closure — nunca no DOM — para que uma
    // remontagem sempre recomece do `classList` real em vez de herdar
    // estado de uma instância anterior já desmontada.
    const pending = new Set<HTMLElement>()

    function reveal(el: HTMLElement) {
      el.classList.add(REVEALED_CLASS)
      pending.delete(el)
      io.unobserve(el)
    }

    const io = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) reveal(entry.target as HTMLElement)
        }
      },
      { threshold: 0, rootMargin: '0px 0px -8% 0px' }
    )

    function observe(el: HTMLElement) {
      // Idempotente por design: se já foi revelado (classe presente, seja de
      // uma montagem anterior ou desta), não há nada a observar.
      if (el.classList.contains(REVEALED_CLASS)) return
      if (!el.style.transitionDelay) {
        el.style.transitionDelay = ((delayIndex++ % 4) * 90) + 'ms'
      }
      pending.add(el)
      io.observe(el)
    }

    function scan() {
      document.querySelectorAll<HTMLElement>(REVEAL_SELECTOR).forEach(observe)
    }

    function checkPending() {
      if (pending.size === 0) return
      const viewportBottom = window.innerHeight || document.documentElement.clientHeight
      pending.forEach((el) => {
        if (el.getBoundingClientRect().top <= viewportBottom) reveal(el)
      })
    }

    scan()
    checkPending()

    // Cobre seções inseridas dinamicamente após a montagem inicial.
    const mo = new MutationObserver(() => {
      if (!disposed) {
        scan()
        checkPending()
      }
    })
    mo.observe(document.documentElement, { childList: true, subtree: true })

    window.addEventListener('scroll', checkPending, { passive: true })
    window.addEventListener('resize', checkPending, { passive: true })

    return () => {
      disposed = true
      io.disconnect()
      mo.disconnect()
      window.removeEventListener('scroll', checkPending)
      window.removeEventListener('resize', checkPending)
    }
  }, [])
}

import '@testing-library/jest-dom/vitest'

// jsdom não implementa IntersectionObserver (usado por `useRevealOnScroll`
// em `src/app/app/`, entre outros usos possíveis). Sem este polyfill, o hook
// cairia sempre no fallback "sem IntersectionObserver" definido em
// produção — o que mascararia regressões no código real do observer.
// Simula intersecção imediata (elemento visível), suficiente para os
// testes de integração (RTL) que só precisam que o componente monte/reaja
// sem lançar `ReferenceError`; o comportamento real de scroll é validado
// via Playwright contra um browser de verdade, não aqui.
if (typeof globalThis.IntersectionObserver === 'undefined') {
  class MockIntersectionObserver implements IntersectionObserver {
    readonly root: Element | Document | null = null
    readonly rootMargin: string = ''
    readonly thresholds: ReadonlyArray<number> = []
    private callback: IntersectionObserverCallback

    constructor(callback: IntersectionObserverCallback) {
      this.callback = callback
    }

    observe(target: Element) {
      this.callback(
        [
          {
            target,
            isIntersecting: true,
            boundingClientRect: target.getBoundingClientRect(),
            intersectionRatio: 1,
            intersectionRect: target.getBoundingClientRect(),
            rootBounds: null,
            time: Date.now(),
          } as IntersectionObserverEntry,
        ],
        this,
      )
    }

    unobserve() {}
    disconnect() {}
    takeRecords(): IntersectionObserverEntry[] {
      return []
    }
  }

  globalThis.IntersectionObserver = MockIntersectionObserver as unknown as typeof IntersectionObserver
}

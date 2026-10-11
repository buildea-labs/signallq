// Geração de ID única compartilhada — antes duplicada linha a linha entre
// `speedEngine.ts` e `telemetry.ts` (mesmo comentário sobre falha no Safari).
export function generateId(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    try {
      return crypto.randomUUID()
    } catch {
      // Falha silenciosa no Safari via HTTP
    }
  }
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

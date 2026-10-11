import type { Metadata } from 'next'
import { PAGE_META } from '@/lib/pageMetaCatalog'
import { routeMetadata } from '@/lib/routeMetadata'
import { SpeedTestContent } from './SpeedTestContent'

export const metadata: Metadata = routeMetadata(PAGE_META['/teste-de-velocidade'])

export default function TesteDeVelocidadePage() {
  return <SpeedTestContent />
}

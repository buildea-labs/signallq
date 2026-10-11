import type { Metadata } from 'next'
import { PAGE_META } from '@/lib/pageMetaCatalog'
import { routeMetadata } from '@/lib/routeMetadata'
import GuiasIndexContent from './GuiasIndexContent'

export const metadata: Metadata = routeMetadata(PAGE_META['/guias'])

export default function GuiasPage() {
  return <GuiasIndexContent />
}

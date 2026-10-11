import type { Metadata } from 'next'
import { PAGE_META } from '@/lib/pageMetaCatalog'
import { routeMetadata } from '@/lib/routeMetadata'
import ComoFuncionaContent from './ComoFuncionaContent'

export const metadata: Metadata = routeMetadata(PAGE_META['/como-funciona'])

export default function ComoFuncionaPage() {
  return <ComoFuncionaContent />
}

import type { Metadata } from 'next'
import { PAGE_META } from '@/lib/pageMetaCatalog'
import { routeMetadata } from '@/lib/routeMetadata'
import DuvidasContent from './DuvidasContent'

export const metadata: Metadata = routeMetadata(PAGE_META['/duvidas'])

export default function DuvidasPage() {
  return <DuvidasContent />
}

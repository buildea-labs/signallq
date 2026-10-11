import type { Metadata } from 'next'
import { PAGE_META } from '../../lib/pageMetaCatalog'
import { routeMetadata } from '../../lib/routeMetadata'
import PrivacyPageContent from './PrivacyPageContent'

export const metadata: Metadata = routeMetadata(PAGE_META['/privacidade'])

export default function PrivacyPage() {
  return <PrivacyPageContent />
}

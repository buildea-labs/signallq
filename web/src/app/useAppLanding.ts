import { SIGNALLQ_PLAY_STORE_URL } from '@/lib/config'
import { FEATURE_DOWNLOAD_APP_CLICADO, trackFeatureUsed } from '@/lib/telemetry'

export function useAppLanding() {
  const baixarNaPlayStore = () => {
    trackFeatureUsed(FEATURE_DOWNLOAD_APP_CLICADO)
    window.open(SIGNALLQ_PLAY_STORE_URL, '_blank', 'noopener,noreferrer')
  }

  return { baixarNaPlayStore }
}

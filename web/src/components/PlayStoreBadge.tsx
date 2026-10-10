"use client"

import Image from 'next/image'
import { SIGNALLQ_PLAY_STORE_URL } from '../lib/config'
import { FEATURE_DOWNLOAD_APP_CLICADO, trackFeatureUsed } from '../lib/telemetry'

interface PlayStoreBadgeProps {
  height?: number
  source: string
}

export function PlayStoreBadge({ height = 44, source }: PlayStoreBadgeProps) {
  const onClick = () => {
    trackFeatureUsed(FEATURE_DOWNLOAD_APP_CLICADO)
    window.open(SIGNALLQ_PLAY_STORE_URL, '_blank', 'noopener,noreferrer')
  }

  const aspectRatio = 866 / 650
  const width = Math.round(height * aspectRatio)

  return (
    <button onClick={onClick} data-source={source} className="block cursor-pointer border-none bg-transparent p-0 leading-none">
      <Image src="/google-play-badge.png" alt="Baixar o SignallQ na Play Store" width={width} height={height} />
    </button>
  )
}

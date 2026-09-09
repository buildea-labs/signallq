package io.signallq.app.ui.ads

import androidx.compose.runtime.staticCompositionLocalOf
import io.signallq.app.ads.AdsTelemetry

/** Instância injetada no host da UI; previews e testes podem permanecer sem telemetria. */
val LocalAdsTelemetry = staticCompositionLocalOf<AdsTelemetry?> { null }

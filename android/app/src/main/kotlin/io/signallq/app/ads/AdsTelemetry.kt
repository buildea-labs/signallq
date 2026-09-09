package io.signallq.app.ads

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

/**
 * Observabilidade mínima do funil de anúncio nativo.
 *
 * Não envia ID de unidade, URL de conteúdo, rede, localização ou qualquer identificador do
 * usuário. A própria coleção do Firebase continua submetida ao consentimento LGPD configurado
 * pela Application; esta classe nunca altera essa política.
 */
class AdsTelemetry
    @Inject
    constructor(
        private val firebaseAnalytics: FirebaseAnalytics,
    ) {
        fun registrarBootstrap(buildHabilitado: Boolean) =
            registrar(stage = "bootstrap", outcome = if (buildHabilitado) "enabled" else "disabled")

        fun registrarConsentimento(
            podeRequisitar: Boolean,
            atualizacaoFalhou: Boolean,
            formularioFalhou: Boolean,
        ) =
            registrar(
                stage = "ump",
                outcome =
                    when {
                        atualizacaoFalhou -> "update_failed"
                        formularioFalhou -> "form_failed"
                        podeRequisitar -> "available"
                        else -> "unavailable"
                    },
            )

        fun registrarFlagsRemotas(resultado: ResultadoFlagsRemotas) =
            registrar(
                stage = "remote_flags",
                outcome = resultado.origem.analyticsId,
                enabledSlots = AdSlot.entries.count { resultado.flags.habilitadoPara(it) },
            )

        fun registrarElegibilidadeInvalida(
            slot: AdSlot?,
            reason: String,
        ) = registrar(stage = "eligibility", outcome = reason, slot = slot)

        fun registrarTentativaDeLoad(slot: AdSlot?) =
            registrar(stage = "load", outcome = "attempt", slot = slot)

        fun registrarResultadoDeLoad(
            slot: AdSlot?,
            outcome: String,
            errorCode: Int? = null,
        ) = registrar(stage = "load", outcome = outcome, slot = slot, errorCode = errorCode)

        private fun registrar(
            stage: String,
            outcome: String,
            slot: AdSlot? = null,
            enabledSlots: Int? = null,
            errorCode: Int? = null,
        ) {
            // Telemetria não pode interferir no bootstrap nem na renderização de anúncios.
            runCatching {
                firebaseAnalytics.logEvent(
                    EVENT_NAME,
                    Bundle().apply {
                        putString(PARAM_STAGE, stage)
                        putString(PARAM_OUTCOME, outcome)
                        slot?.let { putString(PARAM_SLOT, it.name.lowercase()) }
                        enabledSlots?.let { putLong(PARAM_ENABLED_SLOTS, it.toLong()) }
                        errorCode?.let { putLong(PARAM_ERROR_CODE, it.toLong()) }
                    },
                )
            }
        }

        companion object {
            const val EVENT_NAME = "native_ads_pipeline"
            const val PARAM_STAGE = "stage"
            const val PARAM_OUTCOME = "outcome"
            const val PARAM_SLOT = "slot"
            const val PARAM_ENABLED_SLOTS = "enabled_slots"
            const val PARAM_ERROR_CODE = "error_code"
        }
    }

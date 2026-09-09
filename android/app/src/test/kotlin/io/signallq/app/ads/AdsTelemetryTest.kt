package io.signallq.app.ads

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdsTelemetryTest {
    private val firebaseAnalytics = mockk<FirebaseAnalytics>(relaxed = true)
    private val telemetry = AdsTelemetry(firebaseAnalytics)

    @Test
    fun `evento de UMP diferencia atualizacao falha de consentimento indisponivel`() {
        telemetry.registrarConsentimento(podeRequisitar = false, atualizacaoFalhou = true)

        assertEquals("ump", capturarBundle().getString(AdsTelemetry.PARAM_STAGE))
        assertEquals("update_failed", capturarBundle().getString(AdsTelemetry.PARAM_OUTCOME))
    }

    @Test
    fun `evento de falha AdMob inclui apenas slot e codigo`() {
        telemetry.registrarResultadoDeLoad(AdSlot.HISTORICO, outcome = "error", errorCode = 2)

        val bundle = capturarBundle()
        assertEquals("load", bundle.getString(AdsTelemetry.PARAM_STAGE))
        assertEquals("error", bundle.getString(AdsTelemetry.PARAM_OUTCOME))
        assertEquals("historico", bundle.getString(AdsTelemetry.PARAM_SLOT))
        assertEquals(2L, bundle.getLong(AdsTelemetry.PARAM_ERROR_CODE))
        assertFalse(bundle.containsKey("ad_unit_id"))
        assertFalse(bundle.containsKey("content_url"))
    }

    private fun capturarBundle(): Bundle {
        val eventParams = slot<Bundle>()
        verify { firebaseAnalytics.logEvent(AdsTelemetry.EVENT_NAME, capture(eventParams)) }
        return eventParams.captured
    }
}

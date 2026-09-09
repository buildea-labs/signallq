package io.signallq.app.ads

import io.signallq.app.ui.ads.NativeAdIneligibleReason
import io.signallq.app.ui.ads.NativeAdLoadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeAdsGateTest {
    @Test
    fun `build desabilitado bloqueia antes dos demais gates`() {
        val eligibility =
            NativeAdsGate(
                buildEnabled = false,
                umpCanRequestAds = true,
                flags = AdsFlags(masterEnabled = true, velocidade = true),
            ).eligibilityFor(AdSlot.VELOCIDADE)

        assertEquals(
            NativeAdLoadState.Ineligible(NativeAdIneligibleReason.BuildDisabled),
            eligibility.initialState(),
        )
        assertFalse(eligibility.canLoad)
    }

    @Test
    fun `UMP indisponivel nao e confundida com flag desligada`() {
        val eligibility =
            NativeAdsGate(
                buildEnabled = true,
                umpCanRequestAds = false,
                flags = AdsFlags(masterEnabled = true, velocidade = true),
            ).eligibilityFor(AdSlot.VELOCIDADE)

        assertEquals(
            NativeAdLoadState.Ineligible(NativeAdIneligibleReason.ConsentUnavailable),
            eligibility.initialState(),
        )
        assertFalse(eligibility.canLoad)
    }

    @Test
    fun `flag remota desligada permanece distinguivel apos UMP liberar`() {
        val eligibility =
            NativeAdsGate(
                buildEnabled = true,
                umpCanRequestAds = true,
                flags = AdsFlags(masterEnabled = false, velocidade = true),
            ).eligibilityFor(AdSlot.VELOCIDADE)

        assertEquals(
            NativeAdLoadState.Ineligible(NativeAdIneligibleReason.FlagDisabled),
            eligibility.initialState(),
        )
        assertFalse(eligibility.canLoad)
    }

    @Test
    fun `build UMP e flag liberados permitem tentativa de load`() {
        val eligibility =
            NativeAdsGate(
                buildEnabled = true,
                umpCanRequestAds = true,
                flags = AdsFlags(masterEnabled = true, velocidade = true),
            ).eligibilityFor(AdSlot.VELOCIDADE)

        assertEquals(NativeAdLoadState.Loading, eligibility.initialState())
        assertTrue(eligibility.canLoad)
    }
}

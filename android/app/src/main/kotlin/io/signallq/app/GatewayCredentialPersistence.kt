package io.signallq.app

/**
 * Escolhe o destino de persistência só a partir da confirmação autenticada do
 * driver. Sondagem, host e requisito password-only não são suficientes: eles
 * nunca podem substituir uma credencial Nokia legada.
 */
internal fun usaPerfilGatewayIsolado(driverIdConfirmado: String?): Boolean =
    driverIdConfirmado == DRIVER_ID_TP_LINK_ARCHER_C6

internal const val DRIVER_ID_TP_LINK_ARCHER_C6 = "tplink-archer-c6"

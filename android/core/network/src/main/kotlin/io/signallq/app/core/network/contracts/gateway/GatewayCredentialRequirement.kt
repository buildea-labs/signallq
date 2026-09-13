package io.signallq.app.core.network.contracts.gateway

/**
 * Credencial que a pessoa precisa informar para a interface administrativa do
 * equipamento. Isso descreve a interface do fabricante, não o payload de
 * protocolo: alguns firmwares escondem um usuário fixo (por exemplo, o
 * Archer C6 usa `admin` internamente) e não devem pedir um campo fictício na
 * UI.
 */
enum class GatewayCredentialRequirement {
    PASSWORD_ONLY,
    USERNAME_AND_PASSWORD,
}

/** Fonte única para a UI escolher o formulário depois de identificar o driver. */
fun credentialRequirementFor(driverId: String?): GatewayCredentialRequirement =
    DeviceDriverCatalog.entries
        .firstOrNull { it.driverId == driverId }
        ?.credentialRequirement
        // Equipamento sem fingerprint confirmado não deve receber a semântica
        // password-only de outro fabricante por coincidência de endereço IP.
        ?: GatewayCredentialRequirement.USERNAME_AND_PASSWORD

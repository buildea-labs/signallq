package io.signallq.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GatewayCredentialPersistenceTest {
    @Test
    fun `C6 confirmado usa perfil isolado e nao o armazenamento Nokia legado`() {
        assertTrue(usaPerfilGatewayIsolado(DRIVER_ID_TP_LINK_ARCHER_C6))
    }

    @Test
    fun `Nokia e equipamento nao confirmado preservam fluxo legado`() {
        assertFalse(usaPerfilGatewayIsolado("nokia-g1425g-b"))
        assertFalse(usaPerfilGatewayIsolado(null))
    }
}

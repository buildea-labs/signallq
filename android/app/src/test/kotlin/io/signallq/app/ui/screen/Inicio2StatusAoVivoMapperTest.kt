package io.signallq.app.ui.screen

import io.signallq.app.core.diagnostico.EstagioRede
import io.signallq.app.core.diagnostico.StatusEstagio
import io.signallq.app.core.diagnostico.TomDiagnostico
import io.signallq.app.ui.component.SignallQFeedbackTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Inicio2StatusAoVivoMapperTest {
    @Test
    fun `sucesso nos dois estagios gera geral Success e causaPrincipal null`() {
        val status =
            Inicio2StatusAoVivoMapper.mapear(
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.SUCESSO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.SUCESSO),
                ),
            )

        assertEquals(SignallQFeedbackTone.Success, status.geral)
        assertNull(status.causaPrincipal)
        assertEquals(SignallQFeedbackTone.Success, status.porEstagio["Equipamento"])
        assertEquals(SignallQFeedbackTone.Success, status.porEstagio["Wi-Fi"])
        assertEquals(SignallQFeedbackTone.Success, status.porEstagio["Internet"])
    }

    @Test
    fun `erro no Wi-Fi atribui causaPrincipal WIFI e pior caso Error`() {
        val status =
            Inicio2StatusAoVivoMapper.mapear(
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.ERRO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.SUCESSO),
                ),
            )

        assertEquals(SignallQFeedbackTone.Error, status.geral)
        assertEquals(EstagioRede.WIFI, status.causaPrincipal)
        assertEquals(SignallQFeedbackTone.Error, status.porEstagio["Equipamento"])
        assertEquals(SignallQFeedbackTone.Success, status.porEstagio["Internet"])
    }

    @Test
    fun `incerto nos dois estagios nunca atribui causa`() {
        val status =
            Inicio2StatusAoVivoMapper.mapear(
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.INCERTO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.INCERTO),
                ),
            )

        assertEquals(SignallQFeedbackTone.Incerto, status.geral)
        assertNull(status.causaPrincipal)
    }

    @Test
    fun `atencao no provedor com wifi sucesso atribui causaPrincipal PROVEDOR`() {
        val status =
            Inicio2StatusAoVivoMapper.mapear(
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.SUCESSO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.ATENCAO),
                ),
            )

        assertEquals(SignallQFeedbackTone.Warning, status.geral)
        assertEquals(EstagioRede.PROVEDOR, status.causaPrincipal)
    }

    @Test
    fun `lista vazia mapeia para incerto defensivo`() {
        val status = Inicio2StatusAoVivoMapper.mapear(emptyList())

        assertEquals(SignallQFeedbackTone.Incerto, status.geral)
        assertNull(status.causaPrincipal)
    }

    @Test
    fun `incerto() de fallback marca todos os nos conhecidos como Incerto`() {
        val status = Inicio2StatusAoVivoMapper.incerto()

        assertEquals(SignallQFeedbackTone.Incerto, status.geral)
        assertNull(status.causaPrincipal)
        assertEquals(SignallQFeedbackTone.Incerto, status.porEstagio["Equipamento"])
        assertEquals(SignallQFeedbackTone.Incerto, status.porEstagio["Wi-Fi"])
        assertEquals(SignallQFeedbackTone.Incerto, status.porEstagio["Internet"])
    }
}

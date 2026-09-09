package io.signallq.app.feature.speedtest

/**
 * Motivo fechado de uma execução de speedtest não concluir.
 *
 * É um resumo seguro para apresentação. Mensagens de exceções, hostnames e detalhes de
 * protocolo permanecem restritos ao log/telemetria do executor.
 */
enum class CausaFalhaSpeedtest {
    SEM_CONEXAO,
    DNS_OU_HOSTNAME_INACESSIVEL,
    TIMEOUT,
    FALHA_GENERICA,
}

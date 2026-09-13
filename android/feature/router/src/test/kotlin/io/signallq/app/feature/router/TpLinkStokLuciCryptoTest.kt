package io.signallq.app.feature.router

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey

class TpLinkStokLuciCryptoTest {
    @Test
    fun `AES CBC round trip and decimal session material are protocol compatible`() {
        val key = TpLinkStokLuciCrypto.decimalAscii16()
        val iv = TpLinkStokLuciCrypto.decimalAscii16()
        assertTrue(key.matches(Regex("[0-9]{16}")))
        assertTrue(iv.matches(Regex("[0-9]{16}")))
        assertEquals("operation=read", TpLinkStokLuciCrypto.decrypt(key, iv, TpLinkStokLuciCrypto.encrypt(key, iv, "operation=read")))
    }

    @Test
    fun `RSA encryption produces a block for observed public key shape`() {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(1024)
        val publicKey = generator.generateKeyPair().public as RSAPublicKey
        val encrypted =
            TpLinkStokLuciCrypto.rsaPkcs1Hex(
                publicKey.modulus.toString(16),
                publicKey.publicExponent.toString(16),
                "password",
            )
        assertEquals(256, encrypted.length)
    }
}

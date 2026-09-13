@file:Suppress("MagicNumber") // Bit operations and fixed 16-byte protocol requirements.

package io.signallq.app.feature.router

import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Crypto required by the observed stok-luci login protocol. No material escapes a session. */
internal object TpLinkStokLuciCrypto {
    fun decimalAscii16(): String = buildString(16) { repeat(16) { append(SecureRandom().nextInt(10)) } }

    fun md5Hex(value: String): String =
        MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    fun rsaPkcs1Hex(
        modulusHex: String,
        exponentHex: String,
        plaintext: String,
    ): String {
        val key =
            KeyFactory.getInstance("RSA").generatePublic(
                RSAPublicKeySpec(BigInteger(modulusHex, 16), BigInteger(exponentHex, 16)),
            )
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    fun encrypt(
        key: String,
        iv: String,
        plaintext: String,
    ): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv.toByteArray()))
        return Base64Codec.encode(cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)))
    }

    fun decrypt(
        key: String,
        iv: String,
        encrypted: String,
    ): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv.toByteArray()))
        return cipher.doFinal(Base64Codec.decode(encrypted)).toString(Charsets.UTF_8)
    }
}

/** Small Android-API-24-safe Base64 codec; avoids java.util.Base64 (API 26). */
private object Base64Codec {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encode(bytes: ByteArray): String =
        buildString((bytes.size + 2) / 3 * 4) {
            var index = 0
            while (index < bytes.size) {
                val first = bytes[index++].toInt() and 0xff
                val hasSecond = index < bytes.size
                val second = if (hasSecond) bytes[index++].toInt() and 0xff else 0
                val hasThird = index < bytes.size
                val third = if (hasThird) bytes[index++].toInt() and 0xff else 0
                append(ALPHABET[first ushr 2])
                append(ALPHABET[(first and 3 shl 4) or (second ushr 4)])
                append(if (hasSecond) ALPHABET[(second and 15 shl 2) or (third ushr 6)] else '=')
                append(if (hasThird) ALPHABET[third and 63] else '=')
            }
        }

    fun decode(value: String): ByteArray {
        val clean = value.filterNot(Char::isWhitespace)
        require(clean.length % 4 == 0) { "base64 invalido" }
        val out = ArrayList<Byte>((clean.length / 4) * 3)
        clean.chunked(4).forEach { group ->
            val a = digit(group[0])
            val b = digit(group[1])
            val c = if (group[2] == '=') 0 else digit(group[2])
            val d = if (group[3] == '=') 0 else digit(group[3])
            out += ((a shl 2) or (b ushr 4)).toByte()
            if (group[2] != '=') out += (((b and 15) shl 4) or (c ushr 2)).toByte()
            if (group[3] != '=') out += (((c and 3) shl 6) or d).toByte()
        }
        return out.toByteArray()
    }

    private fun digit(char: Char): Int = ALPHABET.indexOf(char).takeIf { it >= 0 } ?: error("base64 invalido")
}

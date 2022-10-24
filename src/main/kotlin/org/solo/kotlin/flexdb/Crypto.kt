package org.solo.kotlin.flexdb

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.security.GeneralSecurityException

object Crypto {
    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun encrypt(text: String, pswd: String): ByteArray {
        return text.encodeToByteArray()
    }

    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun decrypt(text: ByteArray, pswd: String): String {
        return String(text)
    }

    @JvmStatic
    fun hashPassword(string: String): String {
        val argon = Argon2PasswordEncoder()
        return argon.encode(string)
    }

    @JvmStatic
    fun passwordMatches(pswd: String, hashed: String): Boolean {
        val argon = Argon2PasswordEncoder()
        return argon.matches(pswd, hashed)
    }
}


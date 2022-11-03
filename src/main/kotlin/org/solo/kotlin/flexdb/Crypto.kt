package org.solo.kotlin.flexdb

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

object Crypto {
    @JvmStatic
    private val argon = Argon2PasswordEncoder()

    @JvmStatic
    fun hashPassword(string: String): String {
        return argon.encode(string)
    }

    @JvmStatic
    fun passwordMatches(pswd: String, hashed: String): Boolean {
        return argon.matches(pswd, hashed)
    }
}


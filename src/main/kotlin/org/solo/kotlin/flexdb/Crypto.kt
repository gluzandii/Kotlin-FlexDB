package org.solo.kotlin.flexdb

import com.google.crypto.tink.Aead
import org.solo.kotlin.flexdb.GlobalData.handle
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.security.GeneralSecurityException

object Crypto {
    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun encrypt(text: String, pswd: String): ByteArray {
        val aead = handle.getPrimitive(Aead::class.java)
        return aead.encrypt(text.encodeToByteArray(), pswd.encodeToByteArray())
    }

    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun decrypt(text: ByteArray, pswd: String): String {
        val aead = handle.getPrimitive(Aead::class.java)
        return String(aead.decrypt(text, pswd.encodeToByteArray()))
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


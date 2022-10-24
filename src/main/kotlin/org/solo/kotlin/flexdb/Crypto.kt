package org.solo.kotlin.flexdb

import com.google.crypto.tink.Aead
import org.solo.kotlin.flexdb.GlobalData.handle
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
}


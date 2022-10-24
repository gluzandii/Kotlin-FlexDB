package org.solo.kotlin.flexdb.db

import com.fasterxml.jackson.databind.ObjectMapper
import org.solo.kotlin.flexdb.Crypto
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.io.IOException
import java.nio.file.Path
import java.security.GeneralSecurityException
import kotlin.io.path.readBytes
import kotlin.io.path.readText

data class DB(val root: Path, val schema: Path, val logs: Path, val users: Path, val pswd: Path) {
    private val hasher = Argon2PasswordEncoder()

    @Throws(IOException::class, IllegalArgumentException::class, GeneralSecurityException::class)
    fun userExists(name: String, p: String): Boolean {
        val pswdHashed = pswd.readText()
        if (!hasher.matches(p, pswdHashed)) {
            throw IllegalArgumentException("Invalid password provided: $p")
        }

        val decrypted = Crypto.decrypt(users.readBytes(), p)
        val objectMapper = ObjectMapper()

        try {
            val mp = objectMapper.readValue(decrypted, HashMap::class.java) as HashMap<String, Any>
            return mp.containsKey(name)
        } catch (ex: Exception) {
            throw RuntimeException("Unable to parse json data in: $users")
        }
    }
}

package org.solo.kotlin.flexdb.db

import com.fasterxml.jackson.databind.ObjectMapper
import org.solo.kotlin.flexdb.Crypto
import org.solo.kotlin.flexdb.internal.append
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.readText

@Suppress("unused")
class DB(val root: Path, val p: String) {
    var schema: Path
        private set
    var logs: Path
        private set
    var users: Path
        private set
    var pswd: Path
        private set

    init {
        schema = schemafullPath(root)
        logs = logsPath(root)
        users = usersPath(root)
        pswd = pswdPath(root)
    }

    private val hasher = Argon2PasswordEncoder()

    @Throws(Throwable::class)
    fun userExists(name: String): Boolean {
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

    private inline fun tableExists(name: String): Boolean {
        return schema.append(name).isRegularFile()
    }

    fun tablePath(name: String): Path? {
        return if (!tableExists(name)) {
            return null
        } else {
            schema.append(name)
        }
    }
}

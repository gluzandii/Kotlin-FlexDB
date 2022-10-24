package org.solo.kotlin.flexdb.db

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.readText

data class DB(val root: Path, val schema: Path, val logs: Path, val users: Path, val pswd: Path) {
    private val hasher = Argon2PasswordEncoder()

    @Throws(IOException::class, IllegalArgumentException::class)
    fun userExists(name: String, p: String): Boolean {
        val pswdHashed = pswd.readText()
        if (!hasher.matches(p, pswdHashed)) {
            throw IllegalArgumentException("Invalid password provided: $p")
        }
    }
}

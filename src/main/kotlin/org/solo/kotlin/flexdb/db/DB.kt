package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.internal.append
import org.solo.kotlin.flexdb.json.JsonUtil
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText


class DB(val root: Path, val p: String) {
    var schema: Path
        private set
    var index: Path
        private set
    var logs: Path
        private set
    var users: Path
        private set
    var pswd: Path
        private set

    init {
        schema = DbUtil.schemafullPath(root)
        index = DbUtil.indexFullPath(root)
        logs = DbUtil.logsPath(root)
        users = DbUtil.usersPath(root)
        pswd = DbUtil.pswdPath(root)
    }

    private val hasher = Argon2PasswordEncoder()

    fun userExists(name: String): Boolean {
        val pswdHashed = pswd.readText()
        if (!hasher.matches(p, pswdHashed)) {
            throw IllegalArgumentException("Invalid password provided: $p")
        }

        val content = users.readText()
        val mapper = JsonUtil.newBinaryObjectMapper()

        try {
            val mp = mapper.readValue(content, HashMap::class.java) as HashMap<String, Any>
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

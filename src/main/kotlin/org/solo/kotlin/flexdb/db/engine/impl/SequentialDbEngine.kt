package org.solo.kotlin.flexdb.db.engine.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException
import java.nio.file.Files
import java.util.stream.Stream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readBytes

class SequentialDbEngine(db: DB) : DbEngine(db) {
    @Throws(IOException::class)
    override suspend fun loadTable0(tableName: String) {
        if (!db.tableExists(tableName)) {
            throw IOException("Table $tableName does not exist")
        }
        val p = db.schema.resolve(tableName)
        val rgx = Regex("row_\\d+")
        var exp: IOException? = null

        val col = withContext(Dispatchers.IO) {
            return@withContext try {
                p.resolve("column").readBytes()
            } catch (io: IOException) {
                exp = io
                ByteArray(0)
            }
        }

        val dir = withContext(Dispatchers.IO) {
            return@withContext try {
                Files.walk(p)
            } catch (io: IOException) {
                exp = io
                Stream.of()
            }

        }.filter {
            return@filter it.isRegularFile() && it.name != "column"
        }!!

        for (i in dir) {
            if (!rgx.matches(i.name)) {
                continue
            }

            val data = withContext(Dispatchers.IO) {
                return@withContext try {
                    i.readBytes()
                } catch (io: IOException) {
                    exp = io
                    ByteArray(0)
                }
            }
        }
    }

    override suspend fun serializeTable0(table: Table) {
        TODO("Not yet implemented")
    }
}
package org.solo.kotlin.flexdb.zip

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

object ZipUtil {
    @JvmStatic
    private val zipParameters = ZipParameters()

    init {
        zipParameters.compressionMethod = CompressionMethod.DEFLATE
        zipParameters.compressionLevel = CompressionLevel.NORMAL
        zipParameters.isEncryptFiles = true
        zipParameters.encryptionMethod = EncryptionMethod.AES
    }

    @JvmStatic
    @Throws(IOException::class)
    fun compress(output: Path, password: String?, vararg files: File) {
        output.parent!!.createDirectories()

        val zipFile = if (password == null) {
            ZipFile(output.toFile())
        } else {
            ZipFile(output.toFile(), password.toCharArray())
        }

        zipFile.use {
            for (i in files) {
                if (i.isDirectory) {
                    it.addFolder(i, zipParameters)
                    continue
                }

                it.addFile(i, zipParameters)
            }
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun decompress(file: Path, password: String?): List<InZipFile> {
        if (!file.exists()) {
            throw FileNotFoundException("The file does not exist at the path: $file")
        }
        if (!file.isRegularFile()) {
            throw FileNotFoundException("The path: $file does not point to a file.")
        }

        val f = file.toFile()
        val zipFile = if (password == null) {
            ZipFile(f)
        } else {
            ZipFile(f, password.toCharArray())
        }

        val list = mutableListOf<InZipFile>()

        for (i in zipFile.fileHeaders) {
            if (i.isDirectory) {
                continue
            }
            zipFile.getInputStream(i).use {
                val zipIn = it!!
                val bytes = zipIn.readBytes()

                list.add(InZipFile(bytes, i.fileName!!))
            }
        }

        return list
    }
}
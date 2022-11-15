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
    fun decompress(file: Path, password: String?): List<ZipArchiveItem> {
        initZipFile(file, password).use {
            val list = arrayListOf<ZipArchiveItem>()

            for (i in it.fileHeaders) {
                if (i.isDirectory) {
                    continue
                }
                it.getInputStream(i).use { zipIn ->
                    list.add(ZipArchiveItem(zipIn!!.readBytes(), i.fileName!!))
                }
            }

            return list
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun decompressFile(file: Path, password: String?, fileName: String): ZipArchiveItem? {
        initZipFile(file, password).use {
            val header = it.getFileHeader(fileName)

            if (header.isDirectory) {
                return null
            }
            it.getInputStream(header).use { zipIn ->
                return ZipArchiveItem(zipIn!!.readBytes(), header.fileName!!)
            }
        }
    }

    private fun initZipFile(file: Path, password: String?): ZipFile {
        if (!file.exists()) {
            throw FileNotFoundException("The file does not exist at the path: $file")
        }
        if (!file.isRegularFile()) {
            throw FileNotFoundException("The path: $file does not point to a file.")
        }

        val f = file.toFile()
        return if (password == null) {
            ZipFile(f)
        } else {
            ZipFile(f, password.toCharArray())
        }
    }
}
package kin.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class FileWriter(private val file: File,
                 private val append: Boolean = false) : Writer {

    constructor(filename: String): this(File(filename))

    private lateinit var fileChannel: FileChannel
    private var closed = false
    private var initialized = false

    private suspend fun init() = withContext(Dispatchers.IO) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        fileChannel = FileOutputStream(file, append).channel
        initialized = true
    }

    private suspend fun close() = withContext(Dispatchers.IO) {
        closed = true
        fileChannel.force(false)
        fileChannel.close()
    }

    override suspend fun write(bytes: ByteBuf?): Boolean {
        if (closed) return false
        if (!initialized) init()

        if (bytes == null) {
            close()
            return false
        }

        withContext(Dispatchers.IO) { fileChannel.write(bytes.nioBuffer()) }
        bytes.release()
        return true
    }

}

package kin.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class FileWriter(private val file: File,
                 private val append: Boolean = false) : Writer {

    constructor(filename: String): this(File(filename))

    private val fileChannel: FileChannel
    private var closed = false

    init {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        fileChannel = FileOutputStream(file, append).channel
    }

    override suspend fun write(bytes: ByteBuf?): Boolean {
        if (closed) {
            return false
        }

        if (bytes == null) {
            closed = true
            withContext(Dispatchers.IO) {
                fileChannel.force(false)
                fileChannel.close()
            }
            return false
        }

        val byteBuffer = bytes.nioBuffer()
        withContext(Dispatchers.IO) { fileChannel.write(byteBuffer) }
        return true
    }

}

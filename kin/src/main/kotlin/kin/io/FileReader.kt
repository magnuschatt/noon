package kin.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.handler.stream.ChunkedNioFile
import java.io.File

class FileReader(val file: File,
                 private val allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT) : Reader {

    constructor(filename: String): this(File(filename))

    private val chunkedNioFile = ChunkedNioFile(file)

    override suspend fun read(): ByteBuf? {
        return chunkedNioFile.readChunk(allocator)
    }

}

package kin.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.stream.ChunkedInput
import io.netty.handler.stream.ChunkedWriteHandler
import kotlinx.coroutines.sync.Mutex

class ChunkedInputWriter(private val cwh: ChunkedWriteHandler) : ChunkedInput<ByteBuf>, Writer {

    private val writeLock = Mutex()
    private var progress = 0L
    private var closed = false
    private var endOfInput = false
    private var nextChunk: ByteBuf? = null

    override suspend fun write(bytes: ByteBuf?): Boolean {
        writeLock.lock()
        if (endOfInput || closed) {
            writeLock.unlock()
            return false
        }

        if (bytes == null) {
            endOfInput = true
            nextChunk = null
            cwh.resumeTransfer()
            writeLock.unlock()
            return false
        }

        check(nextChunk == null) {
            "next chunk must be null if mutex was unlocked"
        }

        nextChunk = bytes
        cwh.resumeTransfer()
        return true
    }

    override fun progress(): Long {
        return progress
    }

    override fun readChunk(ctx: ChannelHandlerContext?): ByteBuf? {
        return readChunk(ctx?.alloc())
    }

    override fun readChunk(allocator: ByteBufAllocator?): ByteBuf? {
        if (endOfInput) return null
        if (nextChunk == null) return null

        val chunk = nextChunk
        nextChunk = null
        if (chunk != null) {
            writeLock.unlock()
            progress += chunk.readableBytes().toLong()
        }

        return chunk
    }

    override fun length(): Long {
        return -1
    }

    override fun isEndOfInput(): Boolean {
        return endOfInput
    }

    override fun close() {
        closed = true
    }

}
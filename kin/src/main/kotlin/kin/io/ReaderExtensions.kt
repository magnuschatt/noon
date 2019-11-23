package kin.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ClosedReceiveChannelException

suspend fun Reader.readToString(): String {
    val stringWriter = StringWriter()
    this.copyTo(stringWriter)
    return stringWriter.toString()
}

suspend fun Reader.copyTo(writer: Writer) {
    for (bytes in this) {
        writer.write(bytes)
    }
}

operator fun Reader.iterator(): ChannelIterator<ByteBuf> {
    return ReaderIterator(this)
}

class ReaderIterator(private val reader: Reader) : ChannelIterator<ByteBuf> {

    private var next: ByteBuf? = null
    private var done: Boolean = false

    override suspend fun hasNext(): Boolean {
        if (next != null) return false
        if (done) return false

        val read = reader.read()
        next = read

        if (read == null) {
            done = true
            return false
        }

        return true
    }

    override fun next(): ByteBuf {
        val nxt = next
        if (nxt != null) {
            next = null
            return nxt
        }

        throw if (done) ClosedReceiveChannelException("")
        else IllegalStateException("")
    }

}

package kin.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

class StringReader(private val string: String) : Reader {

    var read = false

    override suspend fun read(): ByteBuf? {
        if (!read) {
            read = true
            return Unpooled.copiedBuffer(string, Charsets.UTF_8)
        }
        return null
    }

}

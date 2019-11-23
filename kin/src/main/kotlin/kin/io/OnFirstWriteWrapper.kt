package kin.io

import io.netty.buffer.ByteBuf

class OnFirstWriteWrapper(private val writer: Writer,
                          private val trigger: () -> Unit) : Writer {

    private var first = true

    override suspend fun write(bytes: ByteBuf?): Boolean {
        if (first) {
            first = false
            trigger()
        }
        return writer.write(bytes)
    }
}
package kin.io

import io.netty.buffer.ByteBuf

class EmptyReader : Reader {
    override suspend fun read(): ByteBuf? = null
}
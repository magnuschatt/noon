package kin.io

import io.netty.buffer.ByteBuf

@FunctionalInterface
interface Reader {
    suspend fun read(): ByteBuf?
}
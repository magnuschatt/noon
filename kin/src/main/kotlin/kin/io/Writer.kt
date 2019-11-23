package kin.io

import io.netty.buffer.ByteBuf

@FunctionalInterface
interface Writer {
    suspend fun write(bytes: ByteBuf?): Boolean
}
package kin.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.channels.SendChannel
import java.nio.channels.ClosedChannelException

@Suppress("EXPERIMENTAL_API_USAGE")
data class ChannelWriter(private val channel: SendChannel<ByteBuf>) : Writer {
    override suspend fun write(bytes: ByteBuf?): Boolean = try {
        when {
            bytes == null -> false.also { channel.close() }
            channel.isClosedForSend -> false
            else -> true.also { channel.send(bytes) }
        }
    } catch (exc: ClosedChannelException) {
        false
    }
}
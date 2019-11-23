package kin.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.channels.ReceiveChannel
import java.nio.channels.ClosedChannelException

@Suppress("EXPERIMENTAL_API_USAGE")
data class ChannelReader(private val channel: ReceiveChannel<ByteBuf>) : Reader {
    override suspend fun read(): ByteBuf? = try {
        when {
            channel.isClosedForReceive -> null
            else -> channel.receive()
        }
    } catch (exc: ClosedChannelException) {
        null
    }
}
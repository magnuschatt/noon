package kin.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull

@Suppress("EXPERIMENTAL_API_USAGE")
data class ChannelReader(private val channel: ReceiveChannel<ByteBuf>) : Reader {
    override suspend fun read(): ByteBuf? = channel.receiveOrNull()
}
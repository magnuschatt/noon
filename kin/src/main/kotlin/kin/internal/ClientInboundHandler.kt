package kin.internal

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.sendBlocking

class ClientInboundHandler : SimpleChannelInboundHandler<HttpObject>(false) {

    val httpResponse = CompletableDeferred<HttpResponse>()
    val channel = Channel<ByteBuf>(Channel.UNLIMITED)

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        when (msg) {
            is HttpResponse -> readHttpResponse(msg)
            is HttpContent -> readHttpContent(msg)
            else -> throw IllegalStateException("Unknown message type: $msg")
        }
    }

    private fun readHttpResponse(resp: HttpResponse) {
        httpResponse.complete(resp)
    }

    private fun readHttpContent(msg: HttpContent) {
        try {
            channel.sendBlocking(msg.content())
        } catch (ignored: ClosedSendChannelException) {}
        if (msg is LastHttpContent) {
            channel.close()
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

}
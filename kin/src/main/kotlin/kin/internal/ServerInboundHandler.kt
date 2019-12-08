package kin.internal

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.EXPECT
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.router.Router
import io.netty.handler.stream.ChunkedWriteHandler
import kin.api.Context
import kin.api.Handler
import kin.api.Request
import kin.api.ResponseWriter
import kin.io.ChannelReader
import kin.io.ChannelWriter
import kin.io.ChunkedInputWriter
import kin.io.OnFirstWriteWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import java.net.URI

class ServerInboundHandler(private val router: Router<Handler>) : SimpleChannelInboundHandler<HttpObject>(false) {

    private val channel = Channel<ByteBuf>(1)

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        when (msg) {
            is HttpRequest -> readHttpRequest(ctx, msg)
            is HttpContent -> readHttpContent(msg)
            else -> throw IllegalStateException("Unexpected message type: $msg")
        }
    }

    private fun readHttpContent(msg: HttpContent) {
        try {
            channel.sendBlocking(msg.content())
        } catch (ignored: ClosedSendChannelException) {}
        if (msg is LastHttpContent) {
            channel.close()
        }
    }

    private fun readHttpRequest(ctx: ChannelHandlerContext, msg: HttpRequest) {

        if (msg.headers()[EXPECT] == "100-continue") {
            ctx.writeAndFlush(DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.CONTINUE))
        }

        GlobalScope.launch(Dispatchers.Default) {
            val httpResponse = DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK)
            val cwh = ctx.channel().pipeline()[ChunkedWriteHandler::class.java]
            val chunkedInputWriter = ChunkedInputWriter(cwh)
            val writer = OnFirstWriteWrapper(chunkedInputWriter) {
                ctx.write(httpResponse)
                ctx.writeAndFlush(HttpChunkedInput(chunkedInputWriter)).addListener {
                    ctx.close()
                }
            }

            val method = msg.method()
            val uri = msg.uri()
            val route = router.route(method, uri)
            val request = Request(
                    method = method,
                    uri = URI(uri),
                    body = ChannelReader(channel),
                    pathParams = route.pathParams(),
                    httpVersion = msg.protocolVersion(),
                    headers = msg.headers()
            )
            val response = ResponseWriter(httpResponse, writer)
            val context = Context(request, response, ctx.alloc())
            route.target().handle(context)
            writer.write(null)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}
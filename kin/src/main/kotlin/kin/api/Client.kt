package kin.api

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler
import kin.internal.ClientInboundHandler
import kin.internal.WorkerEventLoopGroup
import kin.internal.suspend
import kin.io.ChannelReader
import kin.io.ChunkedInputWriter
import kin.io.copyTo

class Client {

    private val bootstrap = Bootstrap()

    init {
        bootstrap.group(WorkerEventLoopGroup)
                .channel(NioSocketChannel::class.java)
                .handler(Initializer())
    }

    private inner class Initializer : ChannelInitializer<SocketChannel>() {
        override fun initChannel(ch: SocketChannel) {
            ch.pipeline()
                    .addLast(HttpClientCodec())
                    .addLast(HttpContentDecompressor())
                    .addLast(ChunkedWriteHandler())
                    .addLast(ClientInboundHandler())
        }
    }

    suspend fun execute(request: Request): Response {
        val uri = request.uri
        val ch = bootstrap.connect(uri.host, uri.port).suspend().channel()
        val cwh = ch.pipeline()[ChunkedWriteHandler::class.java]
        val clientHandler = ch.pipeline()[ClientInboundHandler::class.java]
        val nr = DefaultHttpRequest(HttpVersion.HTTP_1_1, request.method, uri.rawPath)
        nr.headers().setAll(request.headers)
        val chunkedInputWriter = ChunkedInputWriter(cwh)
        ch.write(nr)
        ch.writeAndFlush(HttpChunkedInput(chunkedInputWriter))

        request.body.copyTo(chunkedInputWriter)
        chunkedInputWriter.write(null)

        val httpResponse = clientHandler.httpResponse.await()
        val responseBody = ChannelReader(clientHandler.channel)
        ch.closeFuture().suspend()
        return Response(httpResponse.protocolVersion(), httpResponse.status(), httpResponse.headers(), responseBody)
    }

}

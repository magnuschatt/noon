package kin.api

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.router.Router
import io.netty.handler.stream.ChunkedWriteHandler
import kin.internal.BossEventLoopGroup
import kin.internal.DefaultNotFoundHandler
import kin.internal.ServerInboundHandler
import kin.internal.WorkerEventLoopGroup
import mu.KotlinLogging
import java.net.InetSocketAddress

class Server(private val port: Int = 7777) {

    private val logger = KotlinLogging.logger {}
    private val router = Router<Handler>().apply {
        notFound(DefaultNotFoundHandler())
    }

    fun start() {
        startAsync().sync()
    }

    fun startAsync(): ChannelFuture {
        val bootstrap = ServerBootstrap()
                .group(BossEventLoopGroup, WorkerEventLoopGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(Initializer())

        val address = InetSocketAddress(port)
        val ch = bootstrap.bind(address).sync().channel()
        logger.info { "Server is listening on: ${ch.localAddress()}" }
        return ch.closeFuture()
    }

    fun route(method: HttpMethod, pathPattern: String, handler: suspend (Context) -> Unit) {
        route(method, pathPattern, object : Handler {
            override suspend fun handle(ctx: Context) {
                handler(ctx)
            }
        })
    }

    fun route(method: HttpMethod, pathPattern: String, handler: Handler) {
        router.addRoute(method, pathPattern, handler)
    }

    private inner class Initializer : ChannelInitializer<SocketChannel>() {
        override fun initChannel(ch: SocketChannel) {
            ch.pipeline()
                    .addLast(HttpServerCodec())
                    .addLast(ChunkedWriteHandler())
                    .addLast(ServerInboundHandler(router))
        }
    }

}

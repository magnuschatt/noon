package kin.internal

import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.ThreadFactory

val BossEventLoopGroup = NioEventLoopGroup(1, ThreadFactory {
    Thread(it, "GlobalEventLoopGroup").apply {
        isDaemon = true
    }
}).also {
    Runtime.getRuntime().addShutdownHook(Thread {
        it.shutdownGracefully().sync()
    })
}

val WorkerEventLoopGroup = NioEventLoopGroup(0, ThreadFactory {
    Thread(it, "WorkerEventLoopGroup").apply {
        isDaemon = true
    }
}).also {
    Runtime.getRuntime().addShutdownHook(Thread {
        it.shutdownGracefully().sync()
    })
}
package kin.internal

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import kotlinx.coroutines.CompletableDeferred

suspend fun ChannelFuture.suspend(): ChannelFuture {
    val deferred = CompletableDeferred<ChannelFuture>()
    addListener(object : ChannelFutureListener {
        override fun operationComplete(future: ChannelFuture) {
            if (future.isSuccess) deferred.complete(this@suspend)
            else deferred.completeExceptionally(future.cause())
        }
    })
    return deferred.await()
}
package poky

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Kin
import kin.api.Request
import kin.io.readToString
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random

val client = Kin.client()
val eventLoopGroup = NioEventLoopGroup()

fun main() {
    eventLoopGroup.scheduleAtFixedRate(::check, 2000L, 2000L, TimeUnit.MILLISECONDS)
}

fun check() = runBlocking {
    try {
        val key = abs(Random.nextInt()).toString()
        val value = abs(Random.nextInt()).toString()
        val url = "http://tunnel:3333/poky/$key"

        val set = Request(HttpMethod.PUT, url, value)
        set.headers["x-svc"] = "setty"
        set.headers[CONTENT_LENGTH] = value.length
        val setResp = client.execute(set)
        if (setResp.status != HttpResponseStatus.OK) {
            println("FAIL! Set returned status ${setResp.status}")
            return@runBlocking
        }

        val get = Request(HttpMethod.GET, url)
        get.headers["x-svc"] = "setty"
        val getResp = client.execute(get)
        if (getResp.status != HttpResponseStatus.OK) {
            println("FAIL! Get returned status ${getResp.status}")
            return@runBlocking
        }

        val getValue = getResp.body.readToString()
        if (getValue != value) {
            println("FAIL! Returned value did not match: $getValue != $value")
            return@runBlocking
        }

        println("SUCCESS! $key => $value")
    } catch (e: Exception) {
        println("FAIL! ${e.message}")
    }
}
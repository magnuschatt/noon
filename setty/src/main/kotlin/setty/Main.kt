package setty

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpMethod.GET
import io.netty.handler.codec.http.HttpMethod.PUT
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Kin
import kin.io.readToString
import kin.io.writeString
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

val cache = Cache(100)

fun main() = runBlocking {

    val server = Kin.server(7788)

    server.route(PUT, "/:*") { ctx ->
        cache.set(key = ctx.request.uri.path, value = ctx.request.body.readToString())
        ctx.response.status = HttpResponseStatus.OK
    }

    server.route(GET, "/:*") { ctx ->
        val value = cache.get(key = ctx.request.uri.path)
        ctx.response.headers[CONTENT_LENGTH] = 0
        if (value == null) {
            ctx.response.status = HttpResponseStatus.NOT_FOUND
        } else {
            ctx.response.headers[CONTENT_LENGTH] = value.length
            ctx.response.status = HttpResponseStatus.OK
            ctx.response.body.writeString(value)
        }
    }

    server.start()

}
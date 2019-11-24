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

    server.route(PUT, "/:*") { request, response ->
        if (Random.nextInt(100) < 20) {
            response.status = HttpResponseStatus.SERVICE_UNAVAILABLE
        } else {
            cache.set(key = request.uri.path, value = request.body.readToString())
            response.status = HttpResponseStatus.OK
        }
    }

    server.route(GET, "/:*") { request, response ->
        val value = cache.get(key = request.uri.path)
        if (Random.nextInt(100) < 20) {
            response.status = HttpResponseStatus.SERVICE_UNAVAILABLE
        } else if (value == null) {
            response.status = HttpResponseStatus.NOT_FOUND
        } else {
            response.headers[CONTENT_LENGTH] = value.length
            response.status = HttpResponseStatus.OK
            response.body.writeString(value)
        }
    }

    server.start()

}
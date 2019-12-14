package onix

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpMethod.*
import kin.api.Kin
import kin.api.Request
import kin.api.Response
import kin.io.copyTo
import kotlinx.coroutines.runBlocking

const val mantleHost = "http://host:7799"
val client = Kin.client()

fun main() = runBlocking {

    val server = Kin.server(7770)

    server.route(PUT, "/:*") { ctx ->
        val uploadResp = uploadToMantle(ctx.request)
        ctx.response.headers[CONTENT_LENGTH] = 0
        ctx.response.status = uploadResp.status
    }

    server.route(GET, "/:*") { ctx ->
        val downloadResp = downloadFromMantle(ctx.request.uri.path)
        ctx.response.headers[CONTENT_LENGTH] = downloadResp.headers[CONTENT_LENGTH]
        ctx.response.status = downloadResp.status
        downloadResp.body.copyTo(ctx.response.body, close = true)
    }

    server.start()

}

suspend fun downloadFromMantle(path: String): Response {
    val request = Request(GET, "$mantleHost$path")
    return client.execute(request)
}

suspend fun uploadToMantle(inReq: Request): Response {
    val path = inReq.uri.path
    val outReq = Request(PUT, "$mantleHost$path", inReq.body)
    outReq.headers[CONTENT_LENGTH] = inReq.headers[CONTENT_LENGTH]
    return client.execute(outReq)
}

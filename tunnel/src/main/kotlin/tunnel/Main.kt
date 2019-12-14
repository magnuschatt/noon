package tunnel

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Context
import kin.api.Handler
import kin.api.Kin
import kin.api.Request
import kin.io.copyTo
import kin.io.writeString
import java.net.URI

const val serviceHeaderKey = "x-svc"
val client = Kin.client()

fun main() {

    val server = Kin.server(3333)

    server.route(HttpMethod.GET, "/:*", ::handle)
    server.route(HttpMethod.HEAD, "/:*", ::handle)
    server.route(HttpMethod.PUT, "/:*", ::handle)
    server.route(HttpMethod.POST, "/:*", ::handle)
    server.route(HttpMethod.DELETE, "/:*", ::handle)

    server.start()

}

suspend fun handle(ctx: Context) {

    val servicePorts = mapOf(
            "setty" to 7788,
            "poky" to 3322
    )

    val destSvc: String? = ctx.request.headers[serviceHeaderKey]
    if (destSvc == null || destSvc !in servicePorts) {
        ctx.response.status = HttpResponseStatus.BAD_REQUEST
        val content = "missing or unknown $serviceHeaderKey header"
        ctx.response.headers[CONTENT_LENGTH] = content.length
        ctx.response.body.writeString(content)
        return
    }

    val port = servicePorts[destSvc]

    val pathPart = ctx.request.uri.rawPath
    val proxyUrl = URI("http://host:$port$pathPart")
    val proxyRequest = Request(ctx.request.method, proxyUrl, ctx.request.body)
    proxyRequest.headers.set(ctx.request.headers)
    proxyRequest.headers["x-via"] = "tunnel"
    val response = client.execute(proxyRequest)

    ctx.response.status = response.status
    ctx.response.headers.set(response.headers)
    response.body.copyTo(ctx.response.body)

}
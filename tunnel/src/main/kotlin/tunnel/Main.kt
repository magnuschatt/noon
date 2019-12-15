package tunnel

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.*
import kin.io.copyTo
import kin.io.writeString
import java.net.URI

const val serviceHeaderKey = "x-svc"
val client = Kin.client()

fun main() {

    val outServer = Kin.server(3333)
    outServer.route(HttpMethod.GET, "/:*", ::handleOutbound)
    outServer.route(HttpMethod.HEAD, "/:*", ::handleOutbound)
    outServer.route(HttpMethod.PUT, "/:*", ::handleOutbound)
    outServer.route(HttpMethod.POST, "/:*", ::handleOutbound)
    outServer.route(HttpMethod.DELETE, "/:*", ::handleOutbound)
    outServer.startAsync()

    val inServer = Kin.server(3334)
    inServer.route(HttpMethod.GET, "/:*", ::handleInbound)
    inServer.route(HttpMethod.HEAD, "/:*", ::handleInbound)
    inServer.route(HttpMethod.PUT, "/:*", ::handleInbound)
    inServer.route(HttpMethod.POST, "/:*", ::handleInbound)
    inServer.route(HttpMethod.DELETE, "/:*", ::handleInbound)
    inServer.start()

}

suspend fun handleInbound(ctx: Context) {
    val port = 7788
    val proxyRequest = proxyRequest(ctx.request, "http://service:$port")
    val response = client.execute(proxyRequest)
    response.copyTo(ctx.response)
}

suspend fun handleOutbound(ctx: Context) {

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
    val proxyRequest = proxyRequest(ctx.request, "http://host:$port")
    val response = client.execute(proxyRequest)
    response.copyTo(ctx.response)
}

fun proxyRequest(request: Request, newHost: String): Request {
    val pathPart = request.uri.rawPath
    val proxyUrl = URI("$newHost$pathPart")
    val copy = Request(request.method, proxyUrl, request.body)
    copy.headers.set(request.headers)
    copy.headers["x-via"] = "tunnel"
    return copy
}

suspend fun Response.copyTo(responseWriter: ResponseWriter) {
    responseWriter.status = status
    responseWriter.headers.set(headers)
    responseWriter.headers["x-via"] = "tunnel"
    body.copyTo(responseWriter.body)
}
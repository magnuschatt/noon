package kin.example

import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Kin
import kin.io.readToString
import kin.io.writeString

fun main() {
    val server = Kin.server(port = 7777)
    server.route(HttpMethod.GET, "/hello") { ctx ->
        val content = mutableListOf<String>()
        content += "Hello Kin!"
        content += "You sent us this request:"
        content += "${ctx.request.method} ${ctx.request.uri} ${ctx.request.httpVersion}"
        content += ctx.request.headers.map { (k, v) -> "$k: $v" }
        content += ""
        content += ctx.request.body.readToString()
        content += ""
        val contentString = content.joinToString(separator = "\n")

        ctx.response.headers["Content-Length"] = contentString.length.toString()
        ctx.response.status = HttpResponseStatus.OK
        ctx.response.body.writeString(contentString)
    }

    server.start()
}
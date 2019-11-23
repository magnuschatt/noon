package kin.example

import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Kin
import kin.io.readToString
import kin.io.writeString

fun main() {
    val server = Kin.server(port = 7777)
    server.route(HttpMethod.GET, "/hello") { req, resp ->
        val content = mutableListOf<String>()
        content += "Hello Kin!"
        content += "You sent us this request:"
        content += "${req.method} ${req.uri} ${req.httpVersion}"
        content += req.headers.map { (k, v) -> "$k: $v" }
        content += ""
        content += req.body.readToString()
        content += ""
        val contentString = content.joinToString(separator = "\n")

        resp.headers["Content-Length"] = contentString.length.toString()
        resp.status = HttpResponseStatus.OK
        resp.body.writeString(contentString)
    }

    server.start()
}
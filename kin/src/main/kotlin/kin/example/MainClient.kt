package kin.example

import io.netty.handler.codec.http.HttpMethod.GET
import kin.api.Kin
import kin.api.Request
import kin.io.readToString
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = Kin.client()
    val content = "this is the body"
    val request = Request(GET, "http://host:7788/hello", content)
    request.headers["Transfer-Encoding"] = "chunked"
    request.headers["hello"] = "world"
    val response = client.execute(request)

    println("${response.httpVersion} ${response.status}")
    response.headers.forEach { (k, v) -> println("$k: $v") }
    println(response.body.readToString())
}

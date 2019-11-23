package kin.api

import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import kin.io.Writer

class ResponseWriter(private val httpResponse: HttpResponse,
                     val body: Writer) {

    var status: HttpResponseStatus
        get() = httpResponse.status()
        set(value) { httpResponse.status = value }

    val headers: HttpHeaders
        get() = httpResponse.headers()

}
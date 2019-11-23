package kin.internal

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Handler
import kin.api.Request
import kin.api.ResponseWriter
import kin.io.writeString

class DefaultNotFoundHandler : Handler {
    override suspend fun handle(request: Request, response: ResponseWriter) {
        val content = "404 Not Found\n"
        response.headers[HttpHeaderNames.CONTENT_LENGTH] = content.length
        response.status = HttpResponseStatus.NOT_FOUND
        response.body.writeString(content)
    }
}
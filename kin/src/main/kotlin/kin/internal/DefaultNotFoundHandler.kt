package kin.internal

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Context
import kin.api.Handler
import kin.api.Request
import kin.api.ResponseWriter
import kin.io.writeString

class DefaultNotFoundHandler : Handler {
    override suspend fun handle(ctx: Context) {
        val content = "404 Not Found\n"
        ctx.response.headers[HttpHeaderNames.CONTENT_LENGTH] = content.length
        ctx.response.status = HttpResponseStatus.NOT_FOUND
        ctx.response.body.writeString(content)
    }
}
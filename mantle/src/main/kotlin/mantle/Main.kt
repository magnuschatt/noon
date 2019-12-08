package mantle

import io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH
import io.netty.handler.codec.http.HttpMethod.GET
import io.netty.handler.codec.http.HttpMethod.PUT
import io.netty.handler.codec.http.HttpResponseStatus
import kin.api.Kin
import kin.io.copyTo
import kin.io.toReader
import kin.io.toWriter
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {

    val server = Kin.server(7799)
    val dataDir = File("temp/mantle/data/")
    dataDir.mkdirs()

    server.route(PUT, "/:*") { ctx ->
        val file = File(dataDir.absolutePath + ctx.request.uri.path)
        val fileWriter = file.toWriter()
        ctx.request.body.copyTo(fileWriter, close = true)
        ctx.response.headers[CONTENT_LENGTH] = 0
        ctx.response.status = HttpResponseStatus.OK
    }

    server.route(GET, "/:*") { ctx ->
        val file = File(dataDir.absolutePath + ctx.request.uri.path)
        if (!file.exists()) {
            ctx.response.headers[CONTENT_LENGTH] = 0
            ctx.response.status = HttpResponseStatus.NOT_FOUND
            return@route
        }
        val fileReader = file.toReader(ctx.allocator)
        ctx.response.headers[CONTENT_LENGTH] = file.length()
        ctx.response.status = HttpResponseStatus.OK
        fileReader.copyTo(ctx.response.body, close = true)
    }

    server.start()

}
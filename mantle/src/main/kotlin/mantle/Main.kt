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
import java.net.URLEncoder

val dataDir = File("temp/mantle/data/")

fun main() = runBlocking {

    val server = Kin.server(7799)
    dataDir.mkdirs()

    server.route(PUT, "/:*") { ctx ->
        val file = getFileFromPath(ctx.request.uri.path)
        val fileWriter = file.toWriter()
        ctx.request.body.copyTo(fileWriter, close = true)
        ctx.response.headers[CONTENT_LENGTH] = 0
        ctx.response.status = HttpResponseStatus.OK
    }

    server.route(GET, "/:*") { ctx ->
        val file = getFileFromPath(ctx.request.uri.path)
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

fun getFileFromPath(path: String): File {
    val filename = URLEncoder.encode(path, Charsets.UTF_8.toString())
    return File(dataDir.absolutePath + "/" + filename)
}
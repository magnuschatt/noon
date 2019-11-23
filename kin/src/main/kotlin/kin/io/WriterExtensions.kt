package kin.io

import io.netty.buffer.Unpooled
import java.nio.charset.Charset

suspend fun Writer.writeString(string: String, charset: Charset = Charsets.UTF_8): Boolean {
    return write(Unpooled.copiedBuffer(string, charset))
}
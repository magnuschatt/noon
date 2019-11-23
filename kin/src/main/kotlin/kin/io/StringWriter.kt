package kin.io

import io.netty.buffer.ByteBuf
import java.lang.StringBuilder
import java.nio.charset.Charset

class StringWriter(private val charset: Charset = Charsets.UTF_8) : Writer {

    private val sb = StringBuilder()

    override suspend fun write(bytes: ByteBuf?): Boolean {
        if (bytes != null) {
            sb.append(bytes.toString(charset))
            return true
        }
        return false
    }

    override fun toString(): String {
        return sb.toString()
    }

}

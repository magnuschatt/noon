package kin.api

import io.netty.handler.codec.http.*
import kin.io.EmptyReader
import kin.io.Reader
import kin.io.StringReader
import java.net.URI

class Request(val method: HttpMethod,
              val uri: URI,
              val body: Reader,
              val pathParams: Map<String, String> = emptyMap(),
              val httpVersion: HttpVersion = HttpVersion.HTTP_1_1,
              val headers: HttpHeaders = DefaultHttpHeaders()) {

    constructor(method: HttpMethod,
                uriString: String,
                body: Reader = EmptyReader()) : this(method, URI(uriString), body)

    constructor(method: HttpMethod,
                uriString: String,
                body: String) : this(method, URI(uriString), StringReader(body))

}
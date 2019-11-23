package kin.api

import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kin.io.Reader

class Response(val httpVersion: HttpVersion,
               val status: HttpResponseStatus,
               val headers: HttpHeaders,
               val body: Reader)
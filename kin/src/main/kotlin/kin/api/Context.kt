package kin.api

import io.netty.buffer.ByteBufAllocator

class Context(val request: Request,
              val response: ResponseWriter,
              val allocator: ByteBufAllocator)
package kin.io

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import java.io.File

fun File.toReader(allocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT) = FileReader(this, allocator)
fun File.toWriter(append: Boolean = false) = FileWriter(this, append)
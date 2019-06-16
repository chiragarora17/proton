package com.proton.http.server

import java.io.OutputStream

import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultHttpContent

class ChunkOutputStream(ctx: ChannelHandlerContext, chunkSize: Int) extends OutputStream {
  private val allocator = PooledByteBufAllocator.DEFAULT
  private var buffer = allocator.buffer(0 , chunkSize)


  override def close(): Unit = {
    flush()
    super.close()
    buffer.release()
  }

  override def flush(): Unit = {
    if(buffer.readableBytes() > 0) {
      ctx.writeAndFlush(new DefaultHttpContent(buffer))
      buffer = allocator.buffer(0 , chunkSize)
    }
  }


  override def write(b: Int): Unit = {
    if (buffer.maxWritableBytes < 1) {
      flush()
    }
    buffer.writeByte(b)
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    var bufferRemaining = buffer.maxWritableBytes()
    var toWrite = len
    var pos = off
    while (toWrite >= bufferRemaining) {
      buffer.writeBytes(b, pos, bufferRemaining)
      pos += bufferRemaining
      toWrite -= bufferRemaining
      flush()
      bufferRemaining = buffer.maxWritableBytes
    }
    if (toWrite > 0) {
      buffer.writeBytes(b, pos, toWrite)
    }
  }
}

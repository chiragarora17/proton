package com.proton.http.server

import java.net.SocketAddress

import com.proton.json.Json
import com.proton.module.Logger
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpResponseStatus, HttpVersion}

class NebulaExceptionHandler(json: Json) extends ChannelDuplexHandler with Logger {
  override def exceptionCaught(ctx: ChannelHandlerContext, t: Throwable): Unit = {
    logger.error("error in sending exception", t)
    ResponseHandler.getError(t).onSuccess(r => {
      val body = Unpooled.wrappedBuffer(json.toJson(r.body))
      val err = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, body)
      ctx.writeAndFlush(err).addListener(ChannelFutureListener.CLOSE)
    })
    ctx.close()
  }

  override def connect(ctx: ChannelHandlerContext, remoteAddress: SocketAddress, localAddress: SocketAddress, promise: ChannelPromise): Unit = {
    ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener {
      override def operationComplete(f: ChannelFuture) = if(!f.isSuccess) {
        logger.error("unable to connect")
        f.channel().close(promise)
      }
    }))
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    ctx.write(msg, promise.addListener(new ChannelFutureListener {
      override def operationComplete(f: ChannelFuture) = if (!f.isSuccess) {
        logger.error("unable to write")
        f.channel().close(promise)
      }
    }))
  }
}

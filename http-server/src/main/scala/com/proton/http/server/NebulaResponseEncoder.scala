package com.proton.http.server

import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Date

import com.google.common.io.ByteStreams
import com.proton.json.Json
import com.proton.module.Logger
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.handler.codec.http._

class NebulaResponseEncoder(json: Json, config: HttpServerConfig) extends ChannelOutboundHandlerAdapter
  with Logger {
  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    try {
      msg match {
        case (request: Request, response: Response) =>
          val httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, response.code)
          HttpHeaders.setTransferEncodingChunked(httpResponse)
          HttpHeaders.addDateHeader(httpResponse, HttpHeaders.Names.DATE, new Date())
          httpResponse.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8")

          val keepAlive = HttpHeaders.isKeepAlive(request.rawRequest)
          HttpHeaders.setKeepAlive(httpResponse, keepAlive)

          ctx.write(httpResponse)
          response.body match {
            case s: String =>
              ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8))))
            case i: InputStream =>
              val os = new ChunkOutputStream(ctx, config.chunkSize)
              try {
                ByteStreams.copy(i, os)
              } finally {
                os.close()
              }
            case _ =>
              json.toJson(new ChunkOutputStream(ctx, config.chunkSize), response.body)
          }

          if (keepAlive) {
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
          } else {
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE)
          }

        case e: Exception =>
          sendHttpError(ctx, e)
          ctx.close()
        case _ =>
          super.write(ctx, msg, promise)
      }

    } catch {
      case e: Exception =>
        sendHttpError(ctx, e)
        ctx.close()
    }
  }

  private def sendHttpError(ctx: ChannelHandlerContext, e: Exception): Unit = {
    logger.error("error in encoding and sending response", e)
    ResponseHandler.getError(e).map(r => {
      val body = Unpooled.wrappedBuffer(json.toJson(r.body))
      val err = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, body)
      ctx.writeAndFlush(err).addListener(ChannelFutureListener.CLOSE)
    }).get()
  }
}

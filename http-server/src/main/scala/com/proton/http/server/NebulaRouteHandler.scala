package com.proton.http.server

import com.proton.http.server.admin.BuildInfoService
import com.proton.json.Json
import com.proton.module.Logger
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

class NebulaRouteHandler(router: Router, json: Json)
  extends SimpleChannelInboundHandler[FullHttpRequest] with Logger {

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = ctx.flush()

  override def channelRead0(ctx: ChannelHandlerContext, i: FullHttpRequest): Unit = {
    if (HttpHeaders.is100ContinueExpected(i)) {
      ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
    }

    val request = Request(i)(json)
    router.route(request).getOrElse(ResponseHandler.getNotFound).onComplete((r, t) => {
      if (r != null) {
        ctx.writeAndFlush((request, r))
      } else {
        logger.error("error in request", t)
        ResponseHandler.getError(t).onSuccess(r => ctx.writeAndFlush((request, r)))
      }
    })
  }

  object NebulaRouteHandler {
    val properties = BuildInfoService.buildInfo
    val projectArtifactId = properties.getProperty("project.artifactId")
    val projectGroupId = properties.getProperty("project.groupId")
    val projectVersionId = properties.getProperty("project.version")
  }
}

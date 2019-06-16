package com.proton.http.server

import com.proton.config.GlobalConfig
import com.proton.module.Logger
import com.typesafe.config.Config

trait HttpServerConfigProvider {
  val config = new HttpServerConfig
}

object HttpServerConfig {

  import scala.language.implicitConversions

  /**
    * Implicit converts the [[HttpServerConfig]] to a [[com.typesafe.config.Config]] mostly out of convenience so I don't have to do
    * something like config.config.getString(...) instead I can just do config.getString(...)
    */
  implicit def delegate(a: HttpServerConfig): Config = a.config
}

class HttpServerConfig extends GlobalConfig with Logger {
  val numOfIOThreads: Int = config.getOptionalInt("nebula.http.io.threads")
    .getOrElse(Runtime.getRuntime.availableProcessors / 2)

  val maxPostSize: Int = config.getInt("nebula.http.server.post.max.size")

  val chunkSize: Int = config.getInt("nebula.http.server.output.chunk.size")

  val getEventLoop: EventLoopType = config.getOptionalString("nebula.http.eventloop").map(e =>
    if (e.equalsIgnoreCase("epoll")) {
      EventLoopType.Epoll
    } else {
      EventLoopType.Select
    }).getOrElse(defaultEventLoop)

  val markServerAsDaemon: Boolean = config.getOptionalBoolean("nebula.http.server.thread.daemon").getOrElse(false)

  private[server] def defaultEventLoop: EventLoopType = if (io.netty.channel.epoll.Epoll.isAvailable) {
    logger.info("Epoll available using that...")
    EventLoopType.Epoll
  } else {
    logger.info("Epoll not available using NIO...")
    EventLoopType.Select
  }

}

package com.proton.http.server

import java.util.concurrent.ThreadFactory

import com.proton.module.Logger
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{EventLoopGroup, ServerChannel}

trait EventLoopTypeProvider {
  def config: HttpServerConfig

  val eventLoopType: EventLoopType = config.getEventLoop
}

sealed trait EventLoopType {
  private[http] def serverWorkerGroup(numberOfThreads: Int) = eventLoopGroup(numberOfThreads, "netty-server-worker")

  private[http] def bossGroup(setDaemon: Boolean = true): EventLoopGroup = eventLoopGroup(1, "netty-boss-group", setDaemon)

  private[http] def eventLoopGroup(numberOfThreads: Int, name: String, setDaemon: Boolean = true): EventLoopGroup

  private[http] def serverChannel: Class[_ <: ServerChannel]
}

object EventLoopType extends Logger {

  private def workerGroup(name: String, setDaemon: Boolean) = new ThreadFactory {
    def newThread(r: Runnable): Thread = {
      val back: Thread = new Thread(r)
      back.setDaemon(setDaemon)
      back.setName(name)
      back.setPriority(8)
      back
    }
  }

  object Select extends EventLoopType {
    private[http] def eventLoopGroup(numberOfThreads: Int, name: String, setDaemon: Boolean ) = new NioEventLoopGroup(numberOfThreads, workerGroup
    (name, setDaemon))

    private[http] def serverChannel = classOf[NioServerSocketChannel]
  }

  object Epoll extends EventLoopType {
    private[http] def eventLoopGroup(numberOfThreads: Int, name: String, setDaemon: Boolean ) = new EpollEventLoopGroup(numberOfThreads,
      workerGroup(name, setDaemon))

    private[http] def serverChannel = classOf[EpollServerSocketChannel]
  }
}
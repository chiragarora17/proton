package com.proton.http.server

import com.proton.json.Json
import com.proton.module.{Logger, ServiceDescriptor}
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._

trait HttpServerProvider {

  def port: Int

  def config: HttpServerConfig

  def router: Router

  def eventLoopType: EventLoopType

  def jsonTool: Json

  def descriptor: ServiceDescriptor

  lazy val httpServer: HttpServer = new NettyHttpServer(eventLoopType, config, port, router, jsonTool, descriptor)
}

trait HttpServer {
  def start(): Channel
  def stop(): Unit
}

class NettyHttpServer(
    eventLoopType: EventLoopType,
    config: HttpServerConfig,
    port: Int,
    router: Router,
    private[server] val json: Json,
    descriptor: ServiceDescriptor) extends HttpServer with Logger {

  private var channel: Channel = _

  private val workerThreads = eventLoopType.serverWorkerGroup(config.numOfIOThreads)

  private val bossGroup: EventLoopGroup = eventLoopType.bossGroup(config.markServerAsDaemon)

  private val b = new ServerBootstrap()
    .group(bossGroup, workerThreads)
    .channel(eventLoopType.serverChannel)
    .option[java.lang.Integer](ChannelOption.SO_BACKLOG, 128)
    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
    .childOption[java.lang.Integer](ChannelOption.SO_LINGER, -1)
    .childOption[java.lang.Boolean](ChannelOption.SO_REUSEADDR, true)
    .childOption[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)
    .childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
    .childHandler(new ChannelInitializer[SocketChannel] {
      override def initChannel(c: SocketChannel): Unit = {
        c.pipeline()
          .addLast(new HttpRequestDecoder)
          .addLast(new HttpObjectAggregator(config.maxPostSize))
          .addLast(new HttpResponseEncoder)
          .addLast(new HttpContentCompressor)
          .addLast(new NebulaResponseEncoder(json, config))
          .addLast(new NebulaRouteHandler(router, json))
          .addLast(new NebulaExceptionHandler(json))
      }
    })


  override def start(): Channel = {
    channel = b.bind(port).sync().channel()
    channel
  }

  override def stop(): Unit = {
    channel.close().sync()
    channel.closeFuture().sync()
    bossGroup.shutdownGracefully()
    workerThreads.shutdownGracefully()
    descriptor.shutdown()
  }
}

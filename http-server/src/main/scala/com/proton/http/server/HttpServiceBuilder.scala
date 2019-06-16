package com.proton.http.server

import com.google.inject.PrivateModule
import com.proton.config.GlobalConfigModule
import com.proton.http.server.admin.{AdminService, BuildInfoService}
import com.proton.json.{JacksonJsonProvider, Json, JsonModule}
import com.proton.module.{InjectorBuilder, Logger, NebulaModule, ServiceDescriptor}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class HttpServiceBuilder(serviceDescriptor: ServiceDescriptor) extends Logger {
  private val routeClasses = ArrayBuffer[Class[_ <: HttpService]]()
  private val adminRouteClasses = ArrayBuffer[Class[_ <: AdminService]](classOf[BuildInfoService])
  private val modules = ArrayBuffer[PrivateModule]()
  private var jsonToolOpt: Option[Json] = None
  private val serviceDescriptors = ArrayBuffer[ServiceDescriptor](serviceDescriptor)
  private var builderPort: Int = 8080

  def port(port: Int): HttpServiceBuilder = {
    this.builderPort = port
    this
  }

  def serviceDescriptors(descriptor: ServiceDescriptor*): HttpServiceBuilder = {
    this.serviceDescriptors.appendAll(descriptor)
    this
  }

  def modules(module: NebulaModule*): HttpServiceBuilder = {
    this.modules.appendAll(module)
    this
  }

  def jsonTool(jsonTool: Json): HttpServiceBuilder = {
    this.jsonToolOpt = Some(jsonTool)
    this
  }

  def routes(route: Class[_ <: HttpService]*): HttpServiceBuilder = {
    this.routeClasses.appendAll(route)
    this
  }

  def startAndWait(): Unit = {
    val server: HttpServer = this.build()
    logger.info(s"Server started on port $builderPort")
    server.start()
  }

  def build(): HttpServer = {
    logger.info("Starting up...")

    val jsonModule = jsonToolOpt match {
      case Some(json) =>
        new JsonModule {
          override val jsonTool: Json = json
        }
      case None =>
        new JsonModule
    }

    val injector = new InjectorBuilder()
      .addServiceDescriptors(serviceDescriptors.asJava)
      .addModules(jsonModule, new GlobalConfigModule)
      .addModules(modules.asJava)
      .build()

    val adminList = for {
      sc <- adminRouteClasses
      as: AdminService = injector.getInstance(sc)
      rb <- as.routeBuilders
      r = rb.build()
    } yield r

    val routeList = for {
      sc <- routeClasses
      hs: HttpService = injector.getInstance(sc)
      rb <- hs.routeBuilders
      r = rb.build()
    } yield r

    routeList.appendAll(adminList)

    val app = new HttpServerConfigProvider
      with JacksonJsonProvider
      with EventLoopTypeProvider
      with HttpServerProvider
      with RouterProvider {

      override val port: Int = builderPort

      override val basePath: String = serviceDescriptor.serviceName.getPath

      override val routes: ArrayBuffer[Route] = routeList

      override val descriptor: ServiceDescriptor = serviceDescriptor

      override val jsonTool: Json = injector.getInstance(classOf[Json])
    }

    app.httpServer
  }

}

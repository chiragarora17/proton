package com.proton.http.server

import com.proton.http.server.Router.{PatternRouteKey, RouteKey}
import com.proton.module.ServiceResponse
import io.netty.handler.codec.http._

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

trait RouterProvider {
  def basePath: String

  def routes: ArrayBuffer[Route]

  lazy val router = Router(basePath, routes)
}

trait Router {
  def route(request: Request): Option[ServiceResponse[Response]]
}

object Router {
  def apply(basePath: String, routes: ArrayBuffer[Route]): Router = {
    val routeMap: TrieMap[RouteKey, Route] = routes.foldLeft(TrieMap[RouteKey, Route]()) { (m, r) =>
      m.put(RouteKey(r.method, s"/$basePath/${r.version}/${r.path.stripPrefix("/")}"), r)
      m.putIfAbsent(RouteKey(HttpMethod.HEAD, s"/$basePath/${r.version}/${r.path.stripPrefix("/")}"), r)
      if (r.path.endsWith("/")) {
        m.putIfAbsent(RouteKey(r.method, s"/$basePath/${r.version}/${r.path.stripPrefix("/").stripSuffix("/")}"),
          r.copy(path = r.path.stripSuffix("/")))
      }
      m
    }

    val (constantRoutes, nonConstantRoutes) = routeMap.partition { d => d._2.constantRoute }
    val patternRoutes = nonConstantRoutes.map { case (k, v) =>
      PatternRouteKey(k.method, k.path.replaceAll(""":\w+""", """[^/]+""").r) -> v
    }

    new NettyRouter(basePath, constantRoutes, patternRoutes)
  }

  case class RouteKey(method: HttpMethod, path: String)

  case class PatternRouteKey(method: HttpMethod, pathMatcher: Regex)

}

class NettyRouter(basePath: String,
                  routes: TrieMap[RouteKey, Route],
                  patternRoutes: TrieMap[PatternRouteKey, Route]) extends Router {

  override def route(request: Request): Option[ServiceResponse[Response]] = {
    var rtn: Option[ServiceResponse[Response]] = routes.get(RouteKey(request.getMethod, request.getPath))
      .map(r => r.constantHandle(request))

    rtn.orElse {
      patternRoutes.find(d => {
        request.getMethod.equals(d._1.method) && d._1.pathMatcher.pattern.matcher(request.getPath).matches()
      }).flatMap(t => {
        val stripedPath = request.getPath.stripPrefix(s"/$basePath")
        t._2.handle(request, stripedPath)
      })
    }
  }
}

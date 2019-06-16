package com.proton.http.server

import com.proton.module.ServiceResponse
import io.netty.handler.codec.http.HttpMethod

import scala.collection.mutable.ArrayBuffer

class RouteDSL() { self =>

  val routeBuilders = ArrayBuffer[RouteBuilder]()


  def get(route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = get("v1", route)(callback)

  def get(version: String, route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = {
    val routeBuilder = new RouteBuilder(HttpMethod.GET, route, version, callback)
    routeBuilders += routeBuilder
    routeBuilder
  }

  def post(route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = post("v1", route)(callback)

  def post(version: String, route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = {
    val routeBuilder = new RouteBuilder(HttpMethod.POST, route, version, callback)
    routeBuilders += routeBuilder
    routeBuilder
  }

  def put(route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = put("v1", route)(callback)

  def put(version: String, route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = {
    val routeBuilder = new RouteBuilder(HttpMethod.PUT, route, version, callback)
    routeBuilders += routeBuilder
    routeBuilder
  }

  def delete(route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = delete("v1", route)(callback)

  def delete(version: String, route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = {
    val routeBuilder = new RouteBuilder(HttpMethod.DELETE, route, version, callback)
    routeBuilders += routeBuilder
    routeBuilder
  }

  def patch(route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = patch("v1", route)(callback)

  def patch(version: String, route: String)(callback: Request => ServiceResponse[Response]): RouteBuilder = {
    val routeBuilder = new RouteBuilder(HttpMethod.PATCH, route, version, callback)
    routeBuilders += routeBuilder
    routeBuilder
  }
}

package com.proton.http.server

import java.util.function.Supplier

import com.proton.http.server.ResponseHandler.Message
import com.proton.module.ServiceResponse
import io.netty.handler.codec.http._

import scala.util.DynamicVariable

case class Route(method: HttpMethod, path: String, version: String,
                 callback: Request => ServiceResponse[Response], securityCallback: Request => Boolean = request => true) {

  private val pattern = PathPattern(s"/$version/${path.stripPrefix("/")}")

  def summary: String = s"$method-$version-$path"

  def constantRoute: Boolean = captureNames.isEmpty

  def captureNames: Seq[String] = pattern.captureNames

  /**
    * why path here?
    *
    * 1) so we don't have to look it up yet again out of request
    * 2) so I can shorten it; the route functions don't know about the app name that they are all under so I have to strip it
    * out anyway so instead of request knowing about it as well this seemed simpler.
    */
  def handle(request: Request, path: String): Option[ServiceResponse[Response]] = {
    val routeParamOpt = pattern.extract(path)
    if (routeParamOpt.isEmpty) {
      //no path pram then 404 this
      None
    } else {
      // have one ok great make a new request object that contains the path parm map and execute the callback.
      val newRequest = request.copy(pathParams = routeParamOpt)
      Some(constantHandle(newRequest))
    }
  }

  def constantHandle(request: Request): ServiceResponse[Response] = for {
    t <- ServiceResponse.now()
    r <- callbackWithGuiceContext(request)
    _ <- ServiceResponse.now()
  } yield r

  private def callbackWithGuiceContext(request: Request): ServiceResponse[Response] = {
    Route.nebulaRequestDynamicVariable.withValue(request) {
      securityCallback(request) match {
        case true => callback(request)
        case _ => ServiceResponse.now(new Response(HttpResponseStatus.UNAUTHORIZED,
            Message(401, "Unauthorized", HttpResponseStatus.UNAUTHORIZED.reasonPhrase, "error")))
      }
    }
  }
}

object Route {
  val nebulaRequestDynamicVariable = new DynamicVariable[Request](null)

  def legacyRequestScope[A](callback: Supplier[A]): A = {
    Route.nebulaRequestDynamicVariable.withValue(Request()) {
       callback.get()
    }
  }
}

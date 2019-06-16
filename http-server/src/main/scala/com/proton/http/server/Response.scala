package com.proton.http.server

import java.util.concurrent.CompletableFuture

import com.proton.http.server.ResponseHandler.{Message, MessageWithDetails}
import com.proton.module.ServiceResponse
import io.netty.handler.codec.http.HttpResponseStatus

case class Response(code: HttpResponseStatus, body: Any)

object Response {
  def ok(body: Any): ServiceResponse[Response] = makeResponse(HttpResponseStatus.OK, body)
  
  def accepted(body: Any): ServiceResponse[Response] = makeResponse(HttpResponseStatus.ACCEPTED, body)

  def noContent(): ServiceResponse[Response] = makeResponse(HttpResponseStatus.NO_CONTENT, null)

  def created(body: Any = null): ServiceResponse[Response] = makeResponse(HttpResponseStatus.CREATED, body)

  def error(body: String): ServiceResponse[Response] = makeResponse(HttpResponseStatus.BAD_REQUEST,
    Message(HttpResponseStatus.BAD_REQUEST.code(), body, HttpResponseStatus.BAD_REQUEST.reasonPhrase(), "error"))

  def errorWithDetails(body: Any): ServiceResponse[Response] = makeResponse(HttpResponseStatus.BAD_REQUEST,
    MessageWithDetails(HttpResponseStatus.BAD_REQUEST.code(), body, HttpResponseStatus.BAD_REQUEST.reasonPhrase(), "error"))

  private def makeResponse(status: HttpResponseStatus, body: Any): ServiceResponse[Response] = body match {
    case r: ServiceResponse[_] => r.map(d => Response(status, d))
    case f: CompletableFuture[_] => ServiceResponse.wrap(f).map(d => Response(status, d))
    case _ => ServiceResponse.now(Response(status, body))
  }
}

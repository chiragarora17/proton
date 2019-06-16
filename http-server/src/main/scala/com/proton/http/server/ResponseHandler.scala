package com.proton.http.server

import com.proton.module.ServiceResponse
import io.netty.handler.codec.http.HttpResponseStatus

object ResponseHandler {
  case class Message(code: Int, details: String, message: String, status: String)
  case class MessageWithDetails(code: Int, details: Any, message: String, status: String)

  val ERROR="error"

  def getNotFound: ServiceResponse[Response] = ServiceResponse.now(
    new Response(HttpResponseStatus.NOT_FOUND, Message(
      404,
      "The requested URL was not found on the server.",
      "Not Found",
      "error"))
  )

  def getError(t: Throwable): ServiceResponse[Response] = t.getCause match {
    case _:BadRequestException    => ServiceResponse.now(new Response(HttpResponseStatus.BAD_REQUEST,
                                          Message(400, t.getMessage,
                                            HttpResponseStatus.BAD_REQUEST.reasonPhrase, ERROR)))

    case _                        => ServiceResponse.now(new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                          Message(500, t.getMessage,
                                            HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase, ERROR)))

  }

}

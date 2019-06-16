package com.proton.http.server

class BadRequestException (msg: String = null, cause: Throwable = null) extends RuntimeException(msg, cause) {

}


object BadRequestException {
  def apply(msg: String): BadRequestException = new BadRequestException(msg = msg)

  def apply(cause: Throwable): BadRequestException = new BadRequestException(cause = cause)

  def apply(msg: String, cause: Throwable): BadRequestException = new BadRequestException(msg = msg, cause = cause)

}

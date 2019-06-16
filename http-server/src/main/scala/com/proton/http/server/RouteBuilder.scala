package com.proton.http.server

import com.google.common.annotations.VisibleForTesting
import com.proton.module.ServiceResponse
import io.netty.handler.codec.http.HttpMethod

private[http] class RouteBuilder(method: HttpMethod, route: String, version: String,
                                 callback: Request => ServiceResponse[Response]) {

  def build() = Route(method, route, version, callback)

  @VisibleForTesting
  private[server] def getMethod() = method
}

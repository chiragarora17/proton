package com.proton.http.server

import com.google.common.annotations.VisibleForTesting
import com.proton.module.ServiceResponse
import io.netty.handler.codec.http.HttpMethod

private[http] class RouteBuilder(method: HttpMethod, route: String, version: String,
                                 callback: Request => ServiceResponse[Response]) {

  private var securityCallback: Request => Boolean = request => true

  def secure(callback: Request => Boolean = SecurityFilters.apiKeyPathMatchKong) = {
    securityCallback = callback
    this
  }

  def build() = Route(method, route, version, callback, securityCallback)

  @VisibleForTesting
  private[server] def getMethod() = method
}

package com.proton.http.server

object SecurityFilters {

  val apiKeyPathMatchKong: Request => Boolean = (request) =>
    request.getPathParams match {
      case Some(data) =>
        data.get("apiKey") match {
          case Some(apiKey) => apiKey == request.getHeaders.get("X-Consumer-Custom-ID")
          case None => false
        }
      case None => false
    }

  val apiKeyHeaderMatchKong: Request => Boolean = (request) =>
    request.getHeaders.get("X-RR-Api-Key") match {
      case apiKey: String => apiKey == request.getHeaders.get("X-Consumer-Custom-ID")
      case _ => false
    }

  val requireKongApiKey: Request => Boolean = (request) =>
    request.getHeaders.get("X-Consumer-Custom-ID") match {
      case apiKey: String => !apiKey.isEmpty
      case _ => false
    }
}

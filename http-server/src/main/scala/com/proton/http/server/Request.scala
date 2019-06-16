package com.proton.http.server

import java.util.{List => JList, Map => JMap}

import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.io.ByteStreams
import com.google.inject.{Key, Scopes}
import com.proton.json.{JacksonJson, Json}
import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.{DefaultFullHttpRequest, FullHttpRequest, HttpHeaders, HttpMethod, HttpVersion, QueryStringDecoder}

import scala.collection.mutable

case class Request(rawRequest: FullHttpRequest, pathParams: Option[Map[String, String]])
                  (implicit json: Json) {

  private val decodedQueryString = new QueryStringDecoder(rawRequest.getUri)
  private val scopedValuesInternal = mutable.AnyRefMap[Key[_], Any]()
  putScopedValue(Request.KEY, this)

  def getMethod: HttpMethod = rawRequest.getMethod

  def getPath: String = decodedQueryString.path()

  def getHeaders: HttpHeaders = rawRequest.headers()

  def getParams: JMap[String, JList[String]] = decodedQueryString.parameters()

  def getPathParams: Option[Map[String, String]] = pathParams

  def getOrElseUpdateScopedValue[T](key: Key[T])(default: => T): T = synchronized {
    def rawValue(): Any = default match {
      case t if Scopes.isCircularProxy(t) => Request.NULL
      case t => t
    }
    scopedValuesInternal.getOrElseUpdate(key, rawValue) match {
      case Request.NULL => null.asInstanceOf[T]
      case t => t.asInstanceOf[T]
    }
  }

  def putScopedValue[T](key: Key[_ <: T], value: T): Option[Any] = synchronized {
    scopedValuesInternal.put(key, value)
  }

  def body: Option[Array[Byte]] = {
    val buf = rawRequest.content()
    if (buf.isReadable) {
      val dst = Array.ofDim[Byte](buf.readableBytes())
      buf.readBytes(dst)
      Some(dst)
    } else {
      None
    }
  }

  // Allows reading the body multiple times
  private lazy val bodyBytes = ByteStreams.toByteArray(new ByteBufInputStream(rawRequest.content()))

  // Decode Json and return Optional value. Turn any Json errors thrown in the decode operation into a BadRequestException.
  def bodyJsonAs[A](implicit m: Manifest[A]) : Option[A] = {
    try {
      if (bodyBytes.nonEmpty) {
        val obj = json.fromJson[A](bodyBytes)
        Some(obj)
      } else {
        None
      }
    } catch {
      case jpe: JsonProcessingException => throw BadRequestException(jpe.getMessage, jpe)
    }
  }

  def copy(rawRequest: FullHttpRequest = this.rawRequest, pathParams: Option[Map[String, String]] = this.pathParams): Request = {
    Request(rawRequest, pathParams)(json)
  }
}

object Request {
  private val KEY = Key.get(classOf[Request])
  private val NULL = new Object()
  def apply(raw: FullHttpRequest)(implicit json: Json) = new Request(raw, None)(json)

  def apply(): Request = new Request(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""), None)(new JacksonJson)
}

package com.proton.json

import java.io.{InputStream, OutputStream}

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.dataformat.smile.SmileFactory
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JacksonJson {
  val mapper      = new ObjectMapper() with ScalaObjectMapper
  val smileMapper = new ObjectMapper(new SmileFactory()) with ScalaObjectMapper

  private def config (mapper: ObjectMapper with ScalaObjectMapper) = {
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new AfterburnerModule())
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.registerModule(new Jdk8Module())
    mapper.registerModule(new JavaTimeModule())
    mapper.registerModule(new GuavaModule())

  }

  config(mapper)
  config(smileMapper)
}


class JacksonJson(mapper: ObjectMapper with ScalaObjectMapper) extends Json {

  def this() = this(JacksonJson.mapper)

  override def toJson(value: Any): Array[Byte] = mapper.writeValueAsBytes(value)

  override def toJson(outputStream: OutputStream, value: Any): Unit = mapper.writeValue(outputStream, value)

  @throws(classOf[JsonProcessingException])
  override def objToString(value: Any): String = mapper.writeValueAsString(value)

  override def fromJson[T](json: Array[Byte])(implicit m: Manifest[T]): T = mapper.readValue[T](json)

  override def fromJson[T](json: InputStream)(implicit m: Manifest[T]): T = mapper.readValue[T](json)

  override def fromJson[T](json: String)(implicit m: Manifest[T]): T = mapper.readValue[T](json)
}

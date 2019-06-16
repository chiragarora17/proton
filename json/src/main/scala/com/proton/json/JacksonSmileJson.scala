package com.proton.json

import java.io.{IOException, InputStream}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.core.{JsonParseException, JsonProcessingException}
import com.fasterxml.jackson.databind.JsonMappingException

/**
  * Created by carora on 1/26/16.
  */
class JacksonSmileJson extends Smile {
  import JacksonJson._

  @throws(classOf[JsonProcessingException])
  override def serialize(value: Any): Array[Byte] = mapper.writeValueAsBytes(value)

  @throws(classOf[IOException])
  @throws(classOf[JsonParseException])
  @throws(classOf[JsonMappingException])
  override def deserialize[T](json: InputStream, classType: Class[T]): T = mapper.readValue(json, classType)

  @throws(classOf[IOException])
  @throws(classOf[JsonParseException])
  @throws(classOf[JsonMappingException])
  override def deserialize[T](json: Array[Byte], classType: Class[T]): T = mapper.readValue(json, classType)

  @throws(classOf[IOException])
  @throws(classOf[JsonParseException])
  @throws(classOf[JsonMappingException])
  override def deserialize[T](json: Array[Byte], valueTypeRef: TypeReference[T]): T = mapper.readValue(json, valueTypeRef)
}

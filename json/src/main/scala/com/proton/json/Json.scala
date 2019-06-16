package com.proton.json

import java.io.{IOException, InputStream, OutputStream}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.core.{JsonParseException, JsonProcessingException}
import com.fasterxml.jackson.databind.JsonMappingException

trait JsonProvider {
  def jsonTool: Json
  def smileTool: Smile
}

trait Json {
  def toJson(value: Any): Array[Byte]

  def toJson(outputStream: OutputStream, value: Any): Unit

  def objToString(value: Any): String

  def fromJson[T](json: InputStream)(implicit m: Manifest[T]): T

  def fromJson[T](json: Array[Byte])(implicit m: Manifest[T]): T

  def fromJson[T](json: String)(implicit m: Manifest[T]): T
}

trait Smile {

  @throws(classOf[JsonProcessingException])
  def serialize(value: Any): Array[Byte]

  @throws(classOf[IOException])
  @throws(classOf[JsonParseException])
  @throws(classOf[JsonMappingException])
  def deserialize[T](json: InputStream, classType: Class[T]): T

  @throws(classOf[IOException])
  @throws(classOf[JsonParseException])
  @throws(classOf[JsonMappingException])
  def deserialize[T](json: Array[Byte], classType: Class[T]): T

  @throws(classOf[IOException])
  @throws(classOf[JsonParseException])
  @throws(classOf[JsonMappingException])
  def deserialize[T](json: Array[Byte], valueTypeRef: TypeReference[T]): T
}

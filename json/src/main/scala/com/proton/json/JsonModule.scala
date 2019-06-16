package com.proton.json

import com.google.inject.{Exposed, PrivateModule, Provides}

trait JacksonJsonProvider extends JsonProvider {
  override val jsonTool: Json = new JacksonJson
  override val smileTool: Smile = new JacksonSmileJson
}

class JsonModule extends PrivateModule with JacksonJsonProvider {
  override def configure(): Unit = {}

  @Provides
  @Exposed
  def providesJackson(): Json = jsonTool

  @Provides
  @Exposed
  def providesSmile(): Smile = smileTool
}

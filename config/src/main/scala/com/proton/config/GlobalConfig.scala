package com.proton.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.language.implicitConversions

trait GlobalConfig {

  import GlobalConfig._

  val config: Config = realConfig

  implicit def toRichConfig(config: Config): RichConfig = new RichConfig(config)
}

object GlobalConfig {
  private val mainConf = ConfigFactory.load
  private val testConfig = ConfigFactory.parseResourcesAnySyntax("application-test")

  private val realConfig: Config = testConfig.withFallback(mainConf).resolve

  class RichConfig(val underlying: Config) extends AnyVal {
    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) {
      Some(underlying.getString(path))
    } else {
      None
    }

    def getOptionalInt(path: String): Option[Int] = if (underlying.hasPath(path)) {
      Some(underlying.getInt(path))
    } else {
      None
    }

    def getOptionalBoolean(path: String): Option[Boolean] = if (underlying.hasPath(path)) {
      Some(underlying.getBoolean(path))
    } else {
      None
    }
  }

}

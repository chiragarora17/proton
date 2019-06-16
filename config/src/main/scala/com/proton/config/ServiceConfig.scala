package com.proton.config

import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config

class ServiceConfig(serviceName: String) extends AbstractModule with GlobalConfig {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def provideConfig(): Config = config.getConfig(serviceName)
}

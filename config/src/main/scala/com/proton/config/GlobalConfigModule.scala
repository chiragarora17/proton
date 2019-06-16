package com.proton.config

import com.google.inject.AbstractModule
import com.typesafe.config.Config

class GlobalConfigModule extends AbstractModule with GlobalConfig {
  override def configure(): Unit = {
    bind(classOf[Config]).toInstance(config)
  }
}

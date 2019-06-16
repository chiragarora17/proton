package com.proton.config

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.google.inject.Guice
import com.typesafe.config.Config

@RunWith(classOf[JUnitRunner])
class ScalaServiceConfigTest extends Specification {
  "ServiceConfig" >> {
    val injector = Guice.createInjector(new ServiceConfig("service1"))
    val config = injector.getInstance(classOf[Config])

    config.getString("test1") mustEqual "yep"
    config.getInt("test2") mustEqual 2
    config.getInt("test3") mustEqual 55555
    config.getString("test4") mustEqual "service1"
  }
}

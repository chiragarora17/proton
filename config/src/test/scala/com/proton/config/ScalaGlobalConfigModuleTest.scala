package com.proton.config

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.google.inject.Guice
import com.typesafe.config.Config

@RunWith(classOf[JUnitRunner])
class ScalaGlobalConfigModuleTest extends Specification {
  "GlobalConfigModule" >> {
    val injector = Guice.createInjector(new GlobalConfigModule)
    val config: Config = injector.getInstance(classOf[Config])

    config.getString("service1.test1") mustEqual "yep"
    config.getInt("service1.test2") mustEqual 2
    config.getInt("service1.test3") mustEqual 55555
    config.getString("service1.test4") mustEqual "service1"


    config.getString("service2.test1") mustEqual "service2"
    config.getInt("service2.test2") mustEqual 2
    config.getInt("service2.test3") mustEqual 33333
    config.getString("service2.test4") mustEqual "yep"
  }
}

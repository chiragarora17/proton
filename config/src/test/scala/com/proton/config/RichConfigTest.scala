package com.proton.config

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.proton.config.GlobalConfig.RichConfig
import com.typesafe.config.ConfigFactory

@RunWith(classOf[JUnitRunner])
class RichConfigTest extends Specification {
  "RichConfig" >> {
    val mainConf = ConfigFactory.load
    val testConfig = ConfigFactory.parseResourcesAnySyntax("application-test")

    val richConfig = new RichConfig(testConfig.withFallback(mainConf).resolve)

    richConfig.getOptionalString("service1.test1") must beSome("yep")
    richConfig.getOptionalString("service1.ajsklfjasl") must beNone
    richConfig.getOptionalInt("service1.test2") must beSome(2)
    richConfig.getOptionalInt("none.nope") must beNone
    richConfig.getOptionalBoolean("service1.testBoolean") must beSome(true)
    richConfig.getOptionalBoolean("nope.none") must beNone

    val richConfig2 = new RichConfig(ConfigFactory.empty())

    richConfig mustNotEqual richConfig2
    richConfig mustEqual richConfig

  }
}

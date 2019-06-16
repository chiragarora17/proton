package com.proton.config

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class GlobalConfigTest extends Specification {
  "GlobalConfig" should {
    trait GlobalConfigScope extends Scope with GlobalConfig {}
    "GlobalConfig" in new GlobalConfigScope {
      config.getString("service1.test1") mustEqual "yep"
      config.getOptionalString("service1.test1") must beSome("yep")
      config.getOptionalString("service1.ajsklfjasl") must beNone
      config.getOptionalInt("service1.test2") must beSome(2)
      config.getOptionalInt("none.nope") must beNone
    }
  }
}
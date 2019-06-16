package com.proton.module

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.google.inject.Guice

@RunWith(classOf[JUnitRunner])
class ScalaServiceProviderTest extends Specification {
  "ServiceProvider" >> {
    val provider = new ServiceProvider[DumbClass] {
      override def get(): DumbClass = new DumbClass
    }

    val injector = Guice.createInjector(provider)

    val dumbClass = injector.getInstance(classOf[DumbClass])

    dumbClass.getHello mustEqual "Hello"
  }

}

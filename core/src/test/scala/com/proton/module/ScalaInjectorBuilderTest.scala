package com.proton.module

import javax.inject.{Inject, Named}

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.google.inject.name.Names
import com.google.inject.{Exposed, Key, Provides}
import com.netflix.governator.LifecycleInjector
import com.netflix.governator.spi.LifecycleListener

class DumbServiceProvider @Inject()() extends ServiceProvider[DumbClass] {
  override def get(): DumbClass = new DumbClass()
}

@RunWith(classOf[JUnitRunner])
class ScalaInjectorBuilderTest extends Specification {
  "InjectorBuilder" should {
    "work" in {
      val mod1 = new NebulaModule() {
        @Provides
        @Exposed
        @Named("test_string")
        def provideFirststring(): String = "First_String"
      }

      val mod2 = new NebulaModule() {
        @Provides
        @Exposed
        @Named("test_string2")
        def provideSecondString(): String = "Second_String"
      }

      val injector = new InjectorBuilder().addModules(mod1, mod2).build()


      injector.getInstance(Key.get(classOf[String], Names.named("test_string"))) mustEqual "First_String"
      injector.getInstance(Key.get(classOf[String], Names.named("test_string2"))) mustEqual "Second_String"
    }
  }

  "handle lifecycle" in {
    var started = false
    var stopped = false

    val injector = new InjectorBuilder().addLifecycleListener(new LifecycleListener {
      override def onStopped(throwable: Throwable): Unit = stopped = true

      override def onStarted(): Unit = started = true
    }).build()

    injector.asInstanceOf[LifecycleInjector].shutdown()

    started must beTrue
    stopped must beTrue
  }

  "Handle ServiceDescriptors" in {
    val descriptor = new ServiceDescriptor.Builder(ServiceName.of("dumb service"), classOf[DumbServiceProvider]).build()

    val injector = new InjectorBuilder().addServiceDescriptors(descriptor).build()

    val dumbClass = injector.getInstance(classOf[DumbClass])

    dumbClass.getHello mustEqual "Hello"
  }
}

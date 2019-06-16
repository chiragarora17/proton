package com.proton.module

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ScalaServiceResponseTest extends Specification {
  "ServiceResponse" should {
    "create from lazy val" in {
      val rtn = ServiceResponse("test_value")

      rtn.get() mustEqual "test_value"
    }

    "create from failure" in {
      val rtn = ServiceResponse.failure[String](new Exception("bad"))

      rtn.get() must throwA[Exception]
    }

    "lazy is lazy" in {
      def throws(): String = ???
      val rtn = ServiceResponse(throws())

      ok // if we get here, it was lazy
    }

    "timing is right" in {
      val r = for {
        x <- ServiceResponse.now(System.currentTimeMillis())
        y <- ServiceResponse({ println("long computation"); Thread.sleep(100); "done"})
        z <- ServiceResponse.now({ val time = System.currentTimeMillis() - x; println(time); time })
      } yield (y, z)

      println("lazy?")
      Thread.sleep(200)
      println("maybe")
      val (msg, time) = r.get()
      msg mustEqual "done"
      time must beGreaterThanOrEqualTo(100L)
    }

    "can map the value" in {
      val rtn = ServiceResponse.now("test").map(s => s"${s}_mapped")

      rtn.get() mustEqual "test_mapped"
    }

    "can flatmap the value" in {
      val rtn = ServiceResponse("test").flatMap(s => ServiceResponse(s"${s}_flatmapped"))

      rtn.get() mustEqual "test_flatmapped"
    }

    "can use onsuccess" in {
      var ran = false
      val sr = ServiceResponse.now("test_value").onSuccess(s => ran = true)

      ran must beTrue
    }

    "can use oncomplete" in {
      val rtn = ServiceResponse("onCompleteVal").onComplete((s, t) => s)

      rtn.get() mustEqual "onCompleteVal"
    }

    "can recover from failure" in {
      val good = ServiceResponse.now("good").recover(_.getMessage)
      val bad = ServiceResponse.failure[String](new Exception("bad")).recover(_.getMessage)

      good.get() mustEqual "good"
      bad.get() mustEqual "java.lang.Exception: bad"
    }

    "can recover from failure with another ServiceResponse" in {
      val good = ServiceResponse.now("good").recoverWith(ex => ServiceResponse.now(ex.getMessage))
      val bad = ServiceResponse.failure[String](new Exception("bad")).recoverWith(ex => ServiceResponse.now(ex.getMessage))

      good.get() mustEqual "good"
      bad.get() mustEqual "java.lang.Exception: bad"
    }
  }
}

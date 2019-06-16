package com.proton.module

import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LoggerTest extends Specification with Logger {
  "The logger trait" should {
    "accept primitive as logging parameters" in {
      logger.trace("This is an int: {}", 0)
      logger.debug("This is a long: {}", 0L)
      logger.info("This is a boolean: {}", true)
      logger.warn("This is a double: {}", 0.0)
      logger.error("This is a float: {}", 0.0f)
      logger.fatal("This is a char: {}", 'A')
      logger.log(Level.ALL, "This is sparta: {}", 0: Byte, new IllegalArgumentException("Sample exception for logging"))

      success
    }
  }
}
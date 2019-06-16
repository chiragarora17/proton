package com.proton.module

import org.apache.logging.log4j.{Level, LogManager, Logger => JLogger}

import scala.language.implicitConversions


trait Logger {

  import Logger._

  val logger: JLogger = LogManager.getLogger(getClass)

  implicit def ToScalaLogger(logger: JLogger): ScalaLogger = ScalaLogger(logger)

}

object Logger {

  case class ScalaLogger(logger: JLogger) extends AnyVal {
    def debug(msg: String, args: Any*): Unit = logger.debug(msg, args.asInstanceOf[Seq[Object]]: _*)

    def error(msg: String, args: Any*): Unit = logger.error(msg, args.asInstanceOf[Seq[Object]]: _*)

    def fatal(msg: String, args: Any*): Unit = logger.fatal(msg, args.asInstanceOf[Seq[Object]]: _*)

    def info(msg: String, args: Any*): Unit = logger.info(msg, args.asInstanceOf[Seq[Object]]: _*)

    def log(level: Level, msg: String, args: Any*): Unit = logger.log(level, msg, args.asInstanceOf[Seq[Object]]: _*)

    def trace(msg: String, args: Any*): Unit = logger.trace(msg, args.asInstanceOf[Seq[Object]]: _*)

    def warn(msg: String, args: Any*): Unit = logger.warn(msg, args.asInstanceOf[Seq[Object]]: _*)
  }

}
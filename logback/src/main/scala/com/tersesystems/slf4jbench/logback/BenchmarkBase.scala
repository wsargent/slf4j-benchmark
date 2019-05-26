package com.tersesystems.slf4jbench.logback

import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.{ ILoggingEvent, LoggingEvent }
import ch.qos.logback.classic.{ Level, Logger, LoggerContext }
import ch.qos.logback.core.Appender

abstract class BenchmarkBase(resourceName: String) {
  private val context: LoggerContext = contextFromResource(resourceName)

  val logger: Logger = loggerFromContext(context)

  def contextFromResource(resource: String): LoggerContext = {
    val context      = new LoggerContext()
    val configurator = new JoranConfigurator()
    configurator.setContext(context)
    configurator.doConfigure(getClass.getResource(resource))
    context
  }

  def eventFromLogger(logger: Logger, level: Level): LoggingEvent =
    new LoggingEvent(logger.getName, logger, level, "", null, null)

  def loggerFromContext(context: LoggerContext): Logger =
    context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)

  def appenderFromLogger(logger: Logger): Appender[ILoggingEvent] =
    logger.iteratorForAppenders().next()

}

abstract class AppenderBase(resourceName: String, level: Level) extends BenchmarkBase(resourceName) {
  val event: LoggingEvent = eventFromLogger(logger, level)

  val appender: Appender[ILoggingEvent] = appenderFromLogger(logger)
}

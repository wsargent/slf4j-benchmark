package com.tersesystems.slf4jbench.log4j2

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import org.openjdk.jmh.annotations._
import com.sizmek.fsi._

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class SLF4JBenchmark {
  import SLF4JBenchmark._

  /**
    *
    */
  @Benchmark
  def rawDebug(): Unit =
    logger.debug("hello world!")

  /**
    *
    */
  @Benchmark
  def rawDebugWithTemplate(): Unit =
    logger.debug("hello world, {}", longAdder.incrementAndGet())

  /**
    *
    */
  @Benchmark
  def rawDebugWithStringInterpolation(): Unit =
    logger.debug(s"hello world, ${longAdder.incrementAndGet()}")

  /**
    *
    */
  @Benchmark
  def rawDebugWithFastStringInterpolation(): Unit =
    logger.debug(fs"hello world, ${longAdder.incrementAndGet()}")

  /**
    *
    */
  @Benchmark
  def boundedDebugWithTemplate(): Unit =
    if (logger.isDebugEnabled) {
      logger.debug("hello world, {}", longAdder.incrementAndGet())
    }

  /**
    *
    */
  @Benchmark
  def boundedDebugWithStringInterpolation(): Unit =
    if (logger.isDebugEnabled) {
      logger.debug(s"hello world, ${longAdder.incrementAndGet()}")
    }

}

object SLF4JBenchmark {
  val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  val longAdder = new AtomicLong()
}

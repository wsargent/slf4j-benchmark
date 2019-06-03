package com.tersesystems.slf4jbench.logback

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import com.sizmek.fsi._
import org.openjdk.jmh.annotations._

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

object SLF4JBenchmark extends BenchmarkBase("/asyncnop-appender.xml") {
  val longAdder = new AtomicLong()
}

package logbackbenchmark

import org.slf4j._
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import com.sizmek.fsi._

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class LogbackBenchmark {
  import LogbackBenchmark._

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

object LogbackBenchmark {
  private val longAdder = new AtomicLong()

  private val logger = LoggerFactory.getLogger(getClass)
}

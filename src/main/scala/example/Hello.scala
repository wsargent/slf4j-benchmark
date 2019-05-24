package example

import org.slf4j._

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class Hello {
  import Hello._

  @Benchmark
  def boundedDebug: Unit = {
    if (logger.isDebugEnabled) {
      logger.debug("hello world!")
    }
  }
}

object Hello {
    private val logger = LoggerFactory.getLogger(getClass)
}


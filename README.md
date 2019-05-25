# SLF4J Benchmark

## Logback File

```
<configuration>
  <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>

  <appender name="ASYNC" class="net.logstash.logback.appender.LoggingEventAsyncDisruptorAppender">
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>testFile.log</file>
        <append>false</append>
        <immediateFlush>false</immediateFlush>
        <encoder>
        <pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>
        </encoder>
    </appender>
  </appender>

  <root level="DEBUG">
    <appender-ref ref="ASYNC" />
  </root>

</configuration>
```

## Raw Debug

```scala
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

  @Benchmark
  def rawDebug(): Unit =
    logger.debug("hello world!")

  @Benchmark
  def rawDebugWithTemplate(): Unit =
    logger.debug("hello world, {}", longAdder.incrementAndGet())

  @Benchmark
  def rawDebugWithStringInterpolation(): Unit =
    logger.debug(s"hello world, ${longAdder.incrementAndGet()}")

  @Benchmark
  def rawDebugWithFastStringInterpolation(): Unit =
    logger.debug(fs"hello world, ${longAdder.incrementAndGet()}")

  @Benchmark
  def boundedDebugWithTemplate(): Unit =
    if (logger.isDebugEnabled) {
      logger.debug("hello world, {}", longAdder.incrementAndGet())
    }

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
```

## Linux XPS 15

```
$ uname -a
Linux ubuntu-XPS-15-9560 5.0.0-15-generic #16-Ubuntu SMP Mon May 6 17:41:33 UTC 2019 x86_64 x86_64 x86_64 GNU/Linux
```

About:

```
Intel® Core™ i7-7700HQ CPU @ 2.80GHz × 8 
```

### Debug enabled

Run with `sbt clean jmhRun`

```
[info] Benchmark                                             Mode  Cnt    Score    Error  Units
[info] LogbackBenchmark.boundedDebugWithStringInterpolation  avgt   20  221.534 ±  7.482  ns/op
[info] LogbackBenchmark.boundedDebugWithTemplate             avgt   20  367.593 ±  8.630  ns/op
[info] LogbackBenchmark.rawDebug                             avgt   20  222.415 ± 23.730  ns/op
[info] LogbackBenchmark.rawDebugWithFastStringInterpolation  avgt   20  211.223 ±  6.611  ns/op
[info] LogbackBenchmark.rawDebugWithStringInterpolation      avgt   20  297.257 ±  5.526  ns/op
[info] LogbackBenchmark.rawDebugWithTemplate                 avgt   20  366.312 ± 14.276  ns/op
```

### Debug disabled

```
[info] Benchmark                                             Mode  Cnt   Score   Error  Units
[info] LogbackBenchmark.boundedDebugWithStringInterpolation  avgt   20   1.648 ± 0.010  ns/op
[info] LogbackBenchmark.boundedDebugWithTemplate             avgt   20   1.641 ± 0.002  ns/op
[info] LogbackBenchmark.rawDebug                             avgt   20   1.804 ± 0.003  ns/op
[info] LogbackBenchmark.rawDebugWithFastStringInterpolation  avgt   20  43.360 ± 2.278  ns/op
[info] LogbackBenchmark.rawDebugWithStringInterpolation      avgt   20  59.028 ± 0.508  ns/op
[info] LogbackBenchmark.rawDebugWithTemplate                 avgt   20   9.154 ± 0.065  ns/op
```
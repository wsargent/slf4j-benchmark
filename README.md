# SLF4J Benchmark

Benchmarks are run on the following

```text
$ uname -a
Linux ubuntu-XPS-15-9560 5.0.0-15-generic #16-Ubuntu SMP Mon May 6 17:41:33 UTC 2019 x86_64 x86_64 x86_64 GNU/Linux
```

About:

```text
Intel® Core™ i7-7700HQ CPU @ 2.80GHz × 8 
```

[Operation Cost in CPU Cycles](http://ithare.com/infographics-operation-costs-in-cpu-clock-cycles/)

## Logback

### CPU Time

This is a benchmark showing how long it takes to log.

#### Raw Debug

```scala
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

#### Debug enabled with NOP appender.

We'll create an appender that does exactly nothing.

Using the following `logback.xml`:

```xml
<configuration>
  <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>

  <appender name="NOP" class="com.tersesystems.slf4jbench.logback.NoOpAppender">
  </appender>

  <root level="DEBUG">
    <appender-ref ref="NOP" />
  </root>

</configuration>
```

We get the following results:

```text
[info] Benchmark                                           Mode  Cnt   Score   Error  Units
[info] SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  84.019 ± 0.979  ns/op
[info] SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  33.504 ± 0.485  ns/op
[info] SLF4JBenchmark.rawDebug                             avgt   20  24.465 ± 0.323  ns/op
[info] SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  68.601 ± 0.575  ns/op
[info] SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  84.886 ± 0.954  ns/op
[info] SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  33.341 ± 0.574  ns/op
```

#### Debug disabled

With the logger level set to INFO, `isDebugEnabled` returns false, and `rawDebug` also has a check: they both take less than 2 nanoseconds to execute.

Putting a scala string template together takes 60 nanoseconds.  Using the template takes 9.1 nanoseconds.

```text
[info] Benchmark                                             Mode  Cnt   Score   Error  Units
[info] LogbackBenchmark.boundedDebugWithStringInterpolation  avgt   20   1.648 ± 0.010  ns/op
[info] LogbackBenchmark.boundedDebugWithTemplate             avgt   20   1.641 ± 0.002  ns/op
[info] LogbackBenchmark.rawDebug                             avgt   20   1.804 ± 0.003  ns/op
[info] LogbackBenchmark.rawDebugWithFastStringInterpolation  avgt   20  43.360 ± 2.278  ns/op
[info] LogbackBenchmark.rawDebugWithStringInterpolation      avgt   20  59.028 ± 0.508  ns/op
[info] LogbackBenchmark.rawDebugWithTemplate                 avgt   20   9.154 ± 0.065  ns/op
```

So far so good.  We know that whatever happens from this point on, it's probably in the appender.

#### Debug enabled with file

Let's start off with a file appender.  This will block on IO.

Using the following `logback.xml`

```xml
<configuration>
  <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>
  
  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
      <file>testFile.log</file>
      <append>false</append>
      <immediateFlush>false</immediateFlush>
      <encoder>
      <pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>
      </encoder>
  </appender>

  <root level="DEBUG">
    <appender-ref ref="FILE" />
  </root>

</configuration>
```

With a raw file appender that is not flushing immediately, logging takes between 636 and 844 nanoseconds.

```text
[info] Benchmark                                           Mode  Cnt    Score    Error  Units
[info] SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  776.011 ± 62.541  ns/op
[info] SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  871.106 ± 66.052  ns/op
[info] SLF4JBenchmark.rawDebug                             avgt   20  636.405 ± 34.439  ns/op
[info] SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  767.410 ± 68.201  ns/op
[info] SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  819.906 ± 73.166  ns/op
[info] SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  844.242 ± 51.876  ns/op
```

#### Debug enabled with Async Appender

Let's run with an async appender that writes to console.  We use the LoggingEventAsyncDisruptorAppender.

We have have to be careful with this disruptor, because it logs using an internal buffer.  If the buffer fills up completely, the appender will start rejecting log events completely, which makes it really fast in benchmarks, but isn't what we really want.

```xml
<configuration>
  <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>

  <appender name="ASYNC" class="net.logstash.logback.appender.LoggingEventAsyncDisruptorAppender">
    <appender class="ch.qos.logback.core.ConsoleAppender">
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

```text
TODO
```

### Appenders

Appenders are all about throughput.  The throughput that you can generate from Logback is essentially limited by your IO.

You will have larger problems about how to store and process your logs before you run into IO bandwidth constraints.

#### Async with No-Op

Running the `LoggingEventAsyncDisruptorAppender` by itself:

```xml
<configuration>
  <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>

  <appender name="ASYNC" class="net.logstash.logback.appender.LoggingEventAsyncDisruptorAppender">
    <appender class="com.tersesystems.slf4jbench.logback.NoOpAppender">
    </appender>
  </appender>

  <root level="DEBUG">
    <appender-ref ref="ASYNC" />
  </root>

</configuration>
```

```text
[info] Benchmark                                Mode  Cnt     Score     Error   Units
[info] AsyncAppenderBenchmark.appendBenchmark  thrpt   20  3677.492 ± 115.105  ops/ms
```

#### Async with File Appender

The disruptor is lossy, so when the file appender is too slow and the queue fills up, it will start tossing packets over the side -- so it's actually faster.

```xml
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

```text
[info] Benchmark                                    Mode  Cnt      Score     Error   Units
[info] AsyncFileAppenderBenchmark.appendBenchmark  thrpt   20  11879.276 ± 298.794  ops/ms
```

#### File Appender

Running a file appender by itself:

```xml
<configuration>
  <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>

<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>testFile.log</file>
    <append>false</append>
    <immediateFlush>false</immediateFlush>
    <encoder>
    <pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>
    </encoder>
</appender>

  <root level="DEBUG">
    <appender-ref ref="FILE" />
  </root>

</configuration>
```

```text
[info] FileAppenderBenchmark.appendBenchmark       thrpt   20   1789.492 ±  58.814  ops/ms
```

it runs for 5 minutes:

```text
[info] # Run complete. Total time: 00:05:02
```

and generates a very large file:

```text
❱ ls -lah testFile.log          
-rw-r--r-- 1 wsargent wsargent 56G May 25 13:52 testFile.log

❱ wc testFile.log
547633050  2738165250 59144369400 testFile.log
```

# SLF4J Benchmark

This is a benchmark to put some numbers down on the costs and overhead of logging using SLF4J and Logback.

Also checkout my series of posts on Logback:

* [Application Logging in Java: Creating a Logging Framework](https://tersesystems.com/blog/2019/04/23/application-logging-in-java-part-1/)
* [Application Logging in Java: Adding Configuration](https://tersesystems.com/blog/2019/05/05/application-logging-in-java-part-2/)
* [Application Logging in Java: Converters](https://tersesystems.com/blog/2019/05/11/application-logging-in-java-part-3/)
* [Application Logging in Java: Markers](https://tersesystems.com/blog/2019/05/18/application-logging-in-java-part-4/)
* [Application Logging in Java: Appenders](https://tersesystems.com/blog/2019/05/27/application-logging-in-java-part-5/)

## TL;DR

Under normal circumstances, there is not a huge difference between Logback and Log4J 2.  If you're logging in background, use a disruptor based async appender and then log to a buffered filewriter, or to network.  When disabled, logging has effectively no cost.  When enabled, it's effectively free to the application, but it's easy to generate huge amount of logs, and most of the costs involve processing those logs downstream.

**Logging is free, logs are expensive.**

## Abstract
   
### Latency Benchmarks

The JMH test covers the 99.9% average assuming normal distributions, but doesn't worry about spikes or throughput.  In short, we assume that the application doesn't log at a rate of 128K messages/second, is not I/O or GC bound, and doesn't have a backed up CPU work queue, i.e. CPU utilization is less than 70%.

When logging is disabled:

* Using a conditional statement such as `if (logger.isDebugEnabled) { ... }` takes 1.6 nanoseconds.  
* A raw statement such as `logger.debug("hello world")` takes 1.8 nanoseconds.
* A statement that uses string interpolation or string `logger.debug("hello " + name)` takes 60 nanoseconds.

For Logback, when logging is enabled, CPU time depends heavily on the appender:

* With a no-op appender, logging takes between 24 and 84 nanoseconds.
* With a disruptor based async appender logging to no-op, between 150 and 350 nanoseconds.
* With a straight file appender with no immediate flush, between 636 and 850 nanoseconds.

For Log4J 2, CPU time also depends on the appender:

* With a no-op appender, logging takes between 135 and 244 nanoseconds.
* With a disruptor based async appender logging to no-op, between 860 and 1047 nanoseconds.
* With a straight file appender with buffered IO and no immediate flush, between 307 and 405 nanoseconds.

There's no huge difference between Log4J 2 and Logback.  1000 nanoseconds is 0.001 millisecond.  A decent HTTP response takes around 70 - 100 milliseconds, and a decent HTTP framework will process around [10K requests a second](https://info.lightbend.com/white-paper-play-framework-the-jvm-architects-path-to-super-fast-web-app-register.html) on an AWS c4 instance.  If you're using [event based logging](https://www.honeycomb.io/blog/how-are-structured-logs-different-from-events/), then you'll generate 10K logging events per second, per instance, and then you'll also have a smattering of errors and warnings on top of that in production.  

### Throughput Benchmarks

The appenders are measured in terms of throughput, rather than latency.

For logback:

* A disruptor based async appender can perform ~3677 ops/ms against a no-op appender.
* A file appender can perform ~1789 ops/ms, generating 56 GB of data in 5 minutes.
* A disruptor based async appender can perform 11879 ops against a file appender, but that's because it's lossy and will throw things out.

Note that it took five minutes to run through the 56 GB of data with `wc testfile.log` just to count the words.

For Log4J 2:

All of the benchmarks are available in the Log4J 2 source code are available in the [log4j-perf](https://github.com/apache/logging-log4j2/tree/master/log4j-perf) module, so I'll run through that at some point.

> **NOTE**: the comparisons made in [the Log4J 2 benchmark page](https://logging.apache.org/log4j/2.x/performance.html) does against Logback should be disregarded, as they are not up to date or compare apples and oranges, e.g. comparing against `AsyncAppender` instead of [`AsyncDisruptorAppender`](https://github.com/logstash/logstash-logback-encoder/blob/master/src/main/java/net/logstash/logback/appender/AsyncDisruptorAppender.java).

### Conclusions

**Logging is free, logs are expensive.**

Adding a log statement has no appreciable CPU or IO costs.  Even when not contained in a conditional, the overhead is trivial in normal program flow compared to other common operations like switching between threads -- see [Operation Cost in CPU Cycles](http://ithare.com/infographics-operation-costs-in-cpu-clock-cycles/) for more details.

While you can add logs where you feel like, you should not log indiscriminately.  Transmitting, storing, and processing logs is a significant cost for organizations, and so you should keep logging available but disabled so that it is only turned on when there is a need.

There is a case to be made for logging the control flow of every request/response, first noted in [Log Everything All the Time](http://highscalability.com/log-everything-all-time) and popularized by Honeycomb as [event based logging](https://docs.honeycomb.io/learning-about-observability/events-metrics-logs/#events-vs-logs). but with a significant number of events, you may want to use [dynamic sampling](https://www.honeycomb.io/blog/dynamic-sampling-by-example/) to limit processing to only statistically interesting events.

If logging is ever a significant IO overhead, then you are probably logging indiscriminately.  Change your statements from INFO to DEBUG, or from DEBUG to TRACE, or add a marker and filter so that statements are only logged when the marker is applied.

## Platform

Benchmarks are run on a Dell XPS 15 9560.  This is a pretty good laptop, but it's still a laptop.

```text
$ uname -a
Linux ubuntu-XPS-15-9560 5.0.0-15-generic #16-Ubuntu SMP Mon May 6 17:41:33 UTC 2019 x86_64 x86_64 x86_64 GNU/Linux
```

About:

```text
Intel® Core™ i7-7700HQ CPU @ 2.80GHz × 8 
```

## Logback

### Latency Benchmarks

This is a benchmark showing how long it takes to log.

We're not interested in the response time of the system under load. We want to answer the question "how much latency is added by enabling a logging statement with this appender", knowning that [percentile latencies suck](https://theburningmonk.com/2018/10/we-can-do-better-than-percentile-latencies/) in general.  

The JMH test covers the 99.9% average assuming normal distributions, but doesn't worry about spikes or throughput.  In short, we assume that the application doesn't log at a rate of 128K messages/second, is not I/O or GC bound, and doesn't have a backed up CPU work queue, i.e. CPU utilization is less than 70%.

#### Raw Debug

```scala
package com.tersesystems.slf4jbench.logback

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import com.sizmek.fsi._
import org.openjdk.jmh.annotations._
import ch.qos.logback.classic.Level

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class SLF4JBenchmark {
  import SLF4JBenchmark._

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

object SLF4JBenchmark extends BenchmarkBase("/asyncconsole-appender.xml") {
  val longAdder = new AtomicLong()
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

We have have to be careful with this disruptor, because it logs using an internal buffer.  If the buffer fills up completely, the appender will start rejecting log events completely, which makes it really fast in benchmarks, but isn't what we really want.  So we use an async appender to a no-op appender.

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
Benchmark                                           Mode  Cnt    Score    Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  540.145 ±  6.342  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  653.907 ±  9.764  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  420.364 ± 19.027  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  555.187 ± 15.458  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  650.889 ± 24.996  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  727.496 ± 43.636  ns/op
```

### Appenders

Appenders are all about throughput.  The throughput that you can generate from Logback is essentially limited by your IO.

You will have larger problems about how to store and process your logs before you run into IO bandwidth constraints.

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

## Log4J 2

There was no attempt made to run Log4J 2 with special options.  In particular, the `log4j2.enable` settings were left at defaults, to accurately reflect the average user experience.

### Latency Benchmarks

The performance measurements in [Which Log4J2 Appender to Use](https://logging.apache.org/log4j/2.x/performance.html#whichAppender) focus on throughput, rather than latency.  These benchmarks are not the same, because we're
purely looking at how much latency an individual log statement adds to the operation.

There is a section, [Asynchronous Logging Response Time](https://logging.apache.org/log4j/2.x/performance.html#Asynchronous_Logging_Response_Time), but because this is shown as a graph and displayed in milliseconds, it doesn't really give a good detailed look.  

> **NOTE**: the comparisons that Log4J 2 does against Logback should be disregarded, as they are not up to date or compare apples and oranges, e.g. comparing against `AsyncAppender` instead of [`AsyncDisruptorAppender`](https://github.com/logstash/logstash-logback-encoder/blob/master/src/main/java/net/logstash/logback/appender/AsyncDisruptorAppender.java).

The following file was used for latency benchmarks:

```scala
package com.tersesystems.slf4jbench.log4j2

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import org.openjdk.jmh.annotations._
import com.sizmek.fsi._

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class SLF4JBenchmark {
  import SLF4JBenchmark._

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

object SLF4JBenchmark {
  val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  val longAdder = new AtomicLong()
}
```

#### Logging Disabled

Running with logging disabled:

```text
Benchmark                                           Mode  Cnt   Score   Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20   2.307 ± 0.009  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20   2.297 ± 0.004  ns/op
SLF4JBenchmark.rawDebug                             avgt   20   2.311 ± 0.005  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  43.312 ± 0.496  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  61.125 ± 0.754  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20   9.218 ± 0.026  ns/op
```

#### NOP appender

Running with logging enabled and the same no-op appender:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.tersesystems.slf4jbench.log4j2" status="INFO">
    <Appenders>
        <NopAppender name="Nop">
            <PatternLayout pattern="%-4relative [%thread] %-5level %logger{35} - %msg%n"/>
        </NopAppender>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="Nop"/>
        </Root>
    </Loggers>
</Configuration>
```

```text
Benchmark                                           Mode  Cnt    Score   Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  195.839 ± 3.219  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  244.637 ± 3.362  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  135.656 ± 1.254  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  185.214 ± 3.284  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  201.862 ± 2.939  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  222.883 ± 1.918  ns/op
```

#### File appender

Using a [file appender](https://logging.apache.org/log4j/2.x/manual/appenders.html#FileAppender):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.tersesystems.slf4jbench.log4j2" status="INFO">
    <Appenders>
        <File name="MyFile" fileName="app.log">
            <PatternLayout>
                <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
            </PatternLayout>
        </File>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="MyFile"/>
        </Root>
    </Loggers>
</Configuration>
```

Brings an interesting result:

```text
Benchmark                                           Mode  Cnt     Score    Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  2463.822 ± 44.855  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  2445.093 ± 44.728  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  2304.122 ± 65.970  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  2434.949 ± 42.180  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  2442.609 ± 39.417  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  2513.569 ± 45.634  ns/op
```

Using buffered IO set to true and immediate flush set to false:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.tersesystems.slf4jbench.log4j2" status="INFO">
    <Appenders>
        <File name="MyFile" fileName="app.log">
            <PatternLayout>
                <Pattern>%m%n</Pattern>
            </PatternLayout>
            <bufferedIO>true</bufferedIO>
            <immediateFlush>false</immediateFlush>
            <append>false</append>
        </File>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="MyFile"/>
        </Root>
    </Loggers>
</Configuration>
```

then we get much better results:

```text
Benchmark                                           Mode  Cnt    Score    Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  371.541 ±  5.804  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  405.201 ± 25.560  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  307.403 ±  5.618  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  355.634 ±  5.625  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  394.415 ±  4.232  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  392.146 ±  5.961  ns/op
```

#### RandomAccessFile Appender

Using [RandomAccessFileAppender](https://logging.apache.org/log4j/2.x/manual/appenders.html#RandomAccessFileAppender):

```xml
  <Appenders>
    <RandomAccessFile name="MyFile" fileName="logs/app.log">
      <PatternLayout>
        <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
      </PatternLayout>
    </RandomAccessFile>
  </Appenders>
```

shows similar results to the file appender when not using buffered IO:

```text
Benchmark                                           Mode  Cnt     Score     Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  2469.665 ±  75.091  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  2514.523 ±  52.977  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  2276.292 ±  24.440  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  2546.554 ±  72.196  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  2615.754 ± 140.612  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  2618.460 ± 106.518  ns/op
```

Using buffered IO:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.tersesystems.slf4jbench.log4j2" status="INFO">
    <Appenders>
        <RandomAccessFile name="MyFile" fileName="app.log">
            <PatternLayout>
                <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
            </PatternLayout>
            <bufferedIO>true</bufferedIO>
            <immediateFlush>false</immediateFlush>
            <append>false</append>
        </RandomAccessFile>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="MyFile"/>
        </Root>
    </Loggers>
</Configuration>
```

yields a better result, but one that is still slower than the basic FileAppender:

```text
Benchmark                                           Mode  Cnt    Score    Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  850.015 ± 33.969  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  903.224 ± 46.411  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  724.294 ± 32.355  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  786.999 ± 36.753  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  780.606 ± 24.793  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  893.437 ± 64.763  ns/op
```

#### MemoryMappedFileAppender

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.tersesystems.slf4jbench.log4j2" status="INFO">
    <Appenders>
        <MemoryMappedFile name="MyFile" fileName="app.log">
            <PatternLayout>
                <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
            </PatternLayout>
            <immediateFlush>false</immediateFlush>
            <append>false</append>
        </MemoryMappedFile>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="MyFile"/>
        </Root>
    </Loggers>
</Configuration>
```

yields like results as RandomFile appender, still not as good as FileAppender:

```text
Benchmark                                           Mode  Cnt    Score     Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20  862.381 ±  30.928  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  917.074 ± 111.163  ns/op
SLF4JBenchmark.rawDebug                             avgt   20  709.153 ±  54.056  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20  868.345 ±  89.826  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20  832.783 ±  89.799  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20  831.992 ±  78.422  ns/op
```

#### Async Appender and No-op Appender

The [AsyncAppender](https://logging.apache.org/log4j/2.x/manual/async.html) gets a great deal of attention in Log4J 2, and has its own [performance section](https://logging.apache.org/log4j/2.x/manual/async.html#Performance).  However, this is still using milliseconds and is mostly about comparisons, instead of absolute numbers.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.tersesystems.slf4jbench.log4j2" status="INFO">
    <Appenders>
        <NopAppender name="Nop">
            <PatternLayout pattern="%-4relative [%thread] %-5level %logger{35} - %msg%n"/>
        </NopAppender>
        <Async name="Async">
            <AppenderRef ref="Nop"/>
        </Async>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="Async"/>
        </Root>
    </Loggers>
</Configuration>
```

yields:

```text
Benchmark                                           Mode  Cnt     Score    Error  Units
SLF4JBenchmark.boundedDebugWithStringInterpolation  avgt   20   952.120 ± 17.186  ns/op
SLF4JBenchmark.boundedDebugWithTemplate             avgt   20  1047.221 ± 18.245  ns/op
SLF4JBenchmark.rawDebug                             avgt   20   860.273 ± 15.305  ns/op
SLF4JBenchmark.rawDebugWithFastStringInterpolation  avgt   20   872.357 ± 13.884  ns/op
SLF4JBenchmark.rawDebugWithStringInterpolation      avgt   20   968.897 ± 20.770  ns/op
SLF4JBenchmark.rawDebugWithTemplate                 avgt   20   976.926 ± 18.738  ns/op
```

### Appender Throughput

All of the benchmarks are available in the Log4J 2 source code are available in the [log4j-perf](https://github.com/apache/logging-log4j2/tree/master/log4j-perf) module, so I'll run through that at some point.

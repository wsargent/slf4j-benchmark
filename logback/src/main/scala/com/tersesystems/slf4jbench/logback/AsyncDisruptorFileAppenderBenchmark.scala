package com.tersesystems.slf4jbench.logback

import java.util.concurrent.TimeUnit

import ch.qos.logback.classic.Level
import org.openjdk.jmh.annotations._

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class AsyncDisruptorFileAppenderBenchmark {
  import AsyncDisruptorFileAppenderBenchmark._

  @Benchmark
  def appendBenchmark(): Unit =
    appender.doAppend(event)
}

object AsyncDisruptorFileAppenderBenchmark extends AppenderBase("/asyncdisruptorfile-appender.xml", Level.ERROR)
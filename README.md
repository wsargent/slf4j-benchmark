# SLF4J Benchmark

Run with `sbt jmhRun`

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
class Hello {
  import Hello._

  @Benchmark
  def rawDebug: Unit = {
    logger.debug("hello world!")
  }
}
```

With DEBUG level on:

```
[info] Running (fork) org.openjdk.jmh.Main -i 20 -wi 10 -f1 -t1
[info] # JMH version: 1.21
[info] # VM version: JDK 1.8.0_212, OpenJDK 64-Bit Server VM, 25.212-b03
[info] # VM invoker: /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
[info] # VM options: <none>
[info] # Warmup: 10 iterations, 10 s each
[info] # Measurement: 20 iterations, 10 s each
[info] # Timeout: 10 min per iteration
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Average time, time/op
[info] # Benchmark: example.Hello.rawDebug
[info] # Run progress: 0.00% complete, ETA 00:05:00
[info] # Fork: 1 of 1
[info] # Warmup Iteration   1: 162.872 ns/op
[info] # Warmup Iteration   2: 138.909 ns/op
[info] # Warmup Iteration   3: 135.181 ns/op
[info] # Warmup Iteration   4: 137.577 ns/op
[info] # Warmup Iteration   5: 137.230 ns/op
[info] # Warmup Iteration   6: 135.214 ns/op
[info] # Warmup Iteration   7: 138.380 ns/op
[info] # Warmup Iteration   8: 144.694 ns/op
[info] # Warmup Iteration   9: 136.822 ns/op
[info] # Warmup Iteration  10: 139.261 ns/op
[info] Iteration   1: 147.597 ns/op
[info] Iteration   2: 148.342 ns/op
[info] Iteration   3: 147.273 ns/op
[info] Iteration   4: 150.050 ns/op
[info] Iteration   5: 150.857 ns/op
[info] Iteration   6: 155.257 ns/op
[info] Iteration   7: 145.598 ns/op
[info] Iteration   8: 152.014 ns/op
[info] Iteration   9: 152.815 ns/op
[info] Iteration  10: 153.594 ns/op
[info] Iteration  11: 164.123 ns/op
[info] Iteration  12: 158.299 ns/op
[info] Iteration  13: 157.257 ns/op
[info] Iteration  14: 162.867 ns/op
[info] Iteration  15: 158.909 ns/op
[info] Iteration  16: 161.368 ns/op
[info] Iteration  17: 163.290 ns/op
[info] Iteration  18: 157.941 ns/op
[info] Iteration  19: 160.126 ns/op
[info] Iteration  20: 162.534 ns/op
[info] Result "example.Hello.rawDebug":
[info]   155.505 ±(99.9%) 5.183 ns/op [Average]
[info]   (min, avg, max) = (145.598, 155.505, 164.123), stdev = 5.969
[info]   CI (99.9%): [150.323, 160.688] (assumes normal distribution)
[info] # Run complete. Total time: 00:05:00
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark       Mode  Cnt    Score   Error  Units
[info] Hello.rawDebug  avgt   20  155.505 ± 5.183  ns/op
[success] Total time: 305 s, completed May 23, 2019 9:30:30 PM
```

when debug logging is disabled, rawDebug is as follows:

```
[info] Running (fork) org.openjdk.jmh.Main -i 20 -wi 10 -f1 -t1
[info] # JMH version: 1.21
[info] # VM version: JDK 1.8.0_212, OpenJDK 64-Bit Server VM, 25.212-b03
[info] # VM invoker: /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
[info] # VM options: <none>
[info] # Warmup: 10 iterations, 10 s each
[info] # Measurement: 20 iterations, 10 s each
[info] # Timeout: 10 min per iteration
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Average time, time/op
[info] # Benchmark: example.Hello.rawDebug
[info] # Run progress: 0.00% complete, ETA 00:05:00
[info] # Fork: 1 of 1
[info] # Warmup Iteration   1: 1.845 ns/op
[info] # Warmup Iteration   2: 1.828 ns/op
[info] # Warmup Iteration   3: 1.830 ns/op
[info] # Warmup Iteration   4: 1.828 ns/op
[info] # Warmup Iteration   5: 1.825 ns/op
[info] # Warmup Iteration   6: 1.828 ns/op
[info] # Warmup Iteration   7: 1.826 ns/op
[info] # Warmup Iteration   8: 1.827 ns/op
[info] # Warmup Iteration   9: 1.829 ns/op
[info] # Warmup Iteration  10: 1.827 ns/op
[info] Iteration   1: 1.823 ns/op
[info] Iteration   2: 1.827 ns/op
[info] Iteration   3: 1.826 ns/op
[info] Iteration   4: 1.825 ns/op
[info] Iteration   5: 1.823 ns/op
[info] Iteration   6: 1.824 ns/op
[info] Iteration   7: 1.824 ns/op
[info] Iteration   8: 1.827 ns/op
[info] Iteration   9: 1.822 ns/op
[info] Iteration  10: 1.827 ns/op
[info] Iteration  11: 1.826 ns/op
[info] Iteration  12: 1.828 ns/op
[info] Iteration  13: 1.830 ns/op
[info] Iteration  14: 1.831 ns/op
[info] Iteration  15: 1.823 ns/op
[info] Iteration  16: 1.822 ns/op
[info] Iteration  17: 1.828 ns/op
[info] Iteration  18: 1.828 ns/op
[info] Iteration  19: 1.825 ns/op
[info] Iteration  20: 1.880 ns/op
[info] Result "example.Hello.rawDebug":
[info]   1.828 ±(99.9%) 0.011 ns/op [Average]
[info]   (min, avg, max) = (1.822, 1.828, 1.880), stdev = 0.012
[info]   CI (99.9%): [1.818, 1.839] (assumes normal distribution)
[info] # Run complete. Total time: 00:05:00
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark       Mode  Cnt  Score   Error  Units
[info] Hello.rawDebug  avgt   20  1.828 ± 0.011  ns/op
[success] Total time: 305 s, completed May 23, 2019 9:21:30 PM
```

## Bounded Debug

With bounded debug and using as async appender:

```scala
class Hello {
  import Hello._

  @Benchmark
  def boundedDebug: Unit = {
    if (logger.isDebugEnabled) {
      logger.debug("hello world!")
    }
  }
}
```

with debug enabled:

```
[info] Running (fork) org.openjdk.jmh.Main -i 20 -wi 10 -f1 -t1
[info] # JMH version: 1.21
[info] # VM version: JDK 1.8.0_212, OpenJDK 64-Bit Server VM, 25.212-b03
[info] # VM invoker: /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
[info] # VM options: <none>
[info] # Warmup: 10 iterations, 10 s each
[info] # Measurement: 20 iterations, 10 s each
[info] # Timeout: 10 min per iteration
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Average time, time/op
[info] # Benchmark: example.Hello.boundedDebug
[info] # Run progress: 0.00% complete, ETA 00:05:00
[info] # Fork: 1 of 1
[info] # Warmup Iteration   1: 243.247 ns/op
[info] # Warmup Iteration   2: 219.553 ns/op
[info] # Warmup Iteration   3: 218.829 ns/op
[info] # Warmup Iteration   4: 221.036 ns/op
[info] # Warmup Iteration   5: 226.306 ns/op
[info] # Warmup Iteration   6: 229.073 ns/op
[info] # Warmup Iteration   7: 232.682 ns/op
[info] # Warmup Iteration   8: 234.427 ns/op
[info] # Warmup Iteration   9: 238.686 ns/op
[info] # Warmup Iteration  10: 239.226 ns/op
[info] Iteration   1: 241.539 ns/op
[info] Iteration   2: 253.344 ns/op
[info] Iteration   3: 240.265 ns/op
[info] Iteration   4: 241.363 ns/op
[info] Iteration   5: 252.505 ns/op
[info] Iteration   6: 245.642 ns/op
[info] Iteration   7: 254.921 ns/op
[info] Iteration   8: 243.042 ns/op
[info] Iteration   9: 243.734 ns/op
[info] Iteration  10: 252.892 ns/op
[info] Iteration  11: 245.374 ns/op
[info] Iteration  12: 245.821 ns/op
[info] Iteration  13: 260.471 ns/op
[info] Iteration  14: 245.871 ns/op
[info] Iteration  15: 259.133 ns/op
[info] Iteration  16: 248.079 ns/op
[info] Iteration  17: 245.896 ns/op
[info] Iteration  18: 250.655 ns/op
[info] Iteration  19: 251.570 ns/op
[info] Iteration  20: 249.529 ns/op
[info] Result "example.Hello.boundedDebug":
[info]   248.582 ±(99.9%) 5.001 ns/op [Average]
[info]   (min, avg, max) = (240.265, 248.582, 260.471), stdev = 5.759
[info]   CI (99.9%): [243.582, 253.583] (assumes normal distribution)
[info] # Run complete. Total time: 00:05:02
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark           Mode  Cnt    Score   Error  Units
[info] Hello.boundedDebug  avgt   20  248.582 ± 5.001  ns/op
[success] Total time: 308 s, completed May 23, 2019 9:39:24 PM
```

with debug disabled:

```
[info] Running (fork) org.openjdk.jmh.Main -i 20 -wi 10 -f1 -t1
[info] # JMH version: 1.21
[info] # VM version: JDK 1.8.0_212, OpenJDK 64-Bit Server VM, 25.212-b03
[info] # VM invoker: /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
[info] # VM options: <none>
[info] # Warmup: 10 iterations, 10 s each
[info] # Measurement: 20 iterations, 10 s each
[info] # Timeout: 10 min per iteration
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Average time, time/op
[info] # Benchmark: example.Hello.boundedDebug
[info] # Run progress: 0.00% complete, ETA 00:05:00
[info] # Fork: 1 of 1
[info] # Warmup Iteration   1: 1.789 ns/op
[info] # Warmup Iteration   2: 1.664 ns/op
[info] # Warmup Iteration   3: 1.657 ns/op
[info] # Warmup Iteration   4: 1.660 ns/op
[info] # Warmup Iteration   5: 1.654 ns/op
[info] # Warmup Iteration   6: 1.655 ns/op
[info] # Warmup Iteration   7: 1.655 ns/op
[info] # Warmup Iteration   8: 1.654 ns/op
[info] # Warmup Iteration   9: 1.655 ns/op
[info] # Warmup Iteration  10: 1.658 ns/op
[info] Iteration   1: 1.657 ns/op
[info] Iteration   2: 1.662 ns/op
[info] Iteration   3: 1.655 ns/op
[info] Iteration   4: 1.664 ns/op
[info] Iteration   5: 1.658 ns/op
[info] Iteration   6: 1.663 ns/op
[info] Iteration   7: 1.652 ns/op
[info] Iteration   8: 1.653 ns/op
[info] Iteration   9: 1.652 ns/op
[info] Iteration  10: 1.656 ns/op
[info] Iteration  11: 1.654 ns/op
[info] Iteration  12: 1.656 ns/op
[info] Iteration  13: 1.658 ns/op
[info] Iteration  14: 1.655 ns/op
[info] Iteration  15: 1.648 ns/op
[info] Iteration  16: 1.648 ns/op
[info] Iteration  17: 1.651 ns/op
[info] Iteration  18: 1.656 ns/op
[info] Iteration  19: 1.661 ns/op
[info] Iteration  20: 1.693 ns/op
[info] Result "example.Hello.boundedDebug":
[info]   1.658 ±(99.9%) 0.008 ns/op [Average]
[info]   (min, avg, max) = (1.648, 1.658, 1.693), stdev = 0.010
[info]   CI (99.9%): [1.649, 1.666] (assumes normal distribution)
[info] # Run complete. Total time: 00:05:00
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark           Mode  Cnt  Score   Error  Units
[info] Hello.boundedDebug  avgt   20  1.658 ± 0.008  ns/op
[success] Total time: 305 s, completed May 23, 2019 9:46:31 PM
```
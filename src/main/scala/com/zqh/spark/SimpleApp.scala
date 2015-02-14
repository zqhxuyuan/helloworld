package com.zqh.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

/**
 * No Need to start others, just run it as Scala Application on IDEA. you should setMaster, or else will get
 * Caused by: org.apache.spark.SparkException: A master URL must be set in your configuration at ...
 *
 INFO - Changing view acls to: hadoop
 INFO - Changing modify acls to: hadoop
 INFO - SecurityManager: authentication disabled; ui acls disabled; users with view permissions: Set(hadoop); users with modify permissions: Set(hadoop)
 INFO - Slf4jLogger started
 INFO - Starting remoting
 INFO - Remoting started; listening on addresses :[akka.tcp://sparkDriver@localhost:53400]
 INFO - Successfully started service 'sparkDriver' on port 53400.
 INFO - Registering MapOutputTracker
 INFO - Registering BlockManagerMaster
 INFO - Created local directory at /tmp/spark-local-20150214100533-e032
 INFO - MemoryStore started with capacity 1428.6 MB
 WARN - Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
 INFO - HTTP File server directory is /tmp/spark-5c87e6c5-6cf9-4774-9e70-c9d291b9e3ae
 INFO - Starting HTTP Server
 INFO - jetty-8.1.16.v20140903
 INFO - Started SocketConnector@0.0.0.0:56834
 INFO - Successfully started service 'HTTP file server' on port 56834.
 INFO - jetty-8.1.16.v20140903
 INFO - Started SelectChannelConnector@0.0.0.0:4040
 INFO - Successfully started service 'SparkUI' on port 4040.
 INFO - Started SparkUI at http://localhost:4040
 INFO - Connecting to HeartbeatReceiver: akka.tcp://sparkDriver@localhost:53400/user/HeartbeatReceiver
 INFO - Server created on 54016
 INFO - Trying to register BlockManager
 INFO - Registering block manager localhost:54016 with 1428.6 MB RAM, BlockManagerId(<driver>, localhost, 54016)
 INFO - Registered BlockManager
 INFO - ensureFreeSpace(169677) called with curMem=0, maxMem=1497964216
 INFO - Block broadcast_0 stored as values in memory (estimated size 165.7 KB, free 1428.4 MB)
 INFO - ensureFreeSpace(13054) called with curMem=169677, maxMem=1497964216
 INFO - Block broadcast_0_piece0 stored as bytes in memory (estimated size 12.7 KB, free 1428.4 MB)
 INFO - Added broadcast_0_piece0 in memory on localhost:54016 (size: 12.7 KB, free: 1428.6 MB)
 INFO - Updated info of block broadcast_0_piece0
 INFO - Created broadcast 0 from textFile at SimpleApp.scala:13
 INFO - Total input paths to process : 1
 INFO - Starting job: count at SimpleApp.scala:14
 INFO - Got job 0 (count at SimpleApp.scala:14) with 2 output partitions (allowLocal=false)
 INFO - Final stage: Stage 0(count at SimpleApp.scala:14)
 INFO - Parents of final stage: List()
 INFO - Missing parents: List()
 INFO - Submitting Stage 0 (FilteredRDD[2] at filter at SimpleApp.scala:14), which has no missing parents
 INFO - ensureFreeSpace(2768) called with curMem=182731, maxMem=1497964216
 INFO - Block broadcast_1 stored as values in memory (estimated size 2.7 KB, free 1428.4 MB)
 INFO - ensureFreeSpace(1732) called with curMem=185499, maxMem=1497964216
 INFO - Block broadcast_1_piece0 stored as bytes in memory (estimated size 1732.0 B, free 1428.4 MB)
 INFO - Added broadcast_1_piece0 in memory on localhost:54016 (size: 1732.0 B, free: 1428.6 MB)
 INFO - Updated info of block broadcast_1_piece0
 INFO - Created broadcast 1 from broadcast at DAGScheduler.scala:838
 INFO - Submitting 2 missing tasks from Stage 0 (FilteredRDD[2] at filter at SimpleApp.scala:14)
 INFO - Adding task set 0.0 with 2 tasks
 INFO - Starting task 0.0 in stage 0.0 (TID 0, localhost, PROCESS_LOCAL, 1323 bytes)
 INFO - Running task 0.0 in stage 0.0 (TID 0)
 INFO - Partition rdd_1_0 not found, computing it
 INFO - Input split: file:/home/hadoop/soft/cdh5.2.0/spark-1.1.0-cdh5.2.0/README:0+253
 INFO - mapred.tip.id is deprecated. Instead, use mapreduce.task.id
 INFO - mapred.task.id is deprecated. Instead, use mapreduce.task.attempt.id
 INFO - mapred.task.is.map is deprecated. Instead, use mapreduce.task.ismap
 INFO - mapred.task.partition is deprecated. Instead, use mapreduce.task.partition
 INFO - mapred.job.id is deprecated. Instead, use mapreduce.job.id
 INFO - ensureFreeSpace(976) called with curMem=187231, maxMem=1497964216
 INFO - Block rdd_1_0 stored as values in memory (estimated size 976.0 B, free 1428.4 MB)
 INFO - Added rdd_1_0 in memory on localhost:54016 (size: 976.0 B, free: 1428.6 MB)
 INFO - Updated info of block rdd_1_0
 INFO - Finished task 0.0 in stage 0.0 (TID 0). 2326 bytes result sent to driver
 INFO - Starting task 1.0 in stage 0.0 (TID 1, localhost, PROCESS_LOCAL, 1323 bytes)
 INFO - Running task 1.0 in stage 0.0 (TID 1)
 INFO - Partition rdd_1_1 not found, computing it
 INFO - Input split: file:/home/hadoop/soft/cdh5.2.0/spark-1.1.0-cdh5.2.0/README:253+254
 INFO - ensureFreeSpace(616) called with curMem=188207, maxMem=1497964216
 INFO - Block rdd_1_1 stored as values in memory (estimated size 616.0 B, free 1428.4 MB)
 INFO - Added rdd_1_1 in memory on localhost:54016 (size: 616.0 B, free: 1428.6 MB)
 INFO - Updated info of block rdd_1_1
 INFO - Finished task 1.0 in stage 0.0 (TID 1). 2326 bytes result sent to driver
 INFO - Finished task 1.0 in stage 0.0 (TID 1) in 19 ms on localhost (1/2)
 INFO - Finished task 0.0 in stage 0.0 (TID 0) in 134 ms on localhost (2/2)
 INFO - Stage 0 (count at SimpleApp.scala:14) finished in 0.156 s
 INFO - Removed TaskSet 0.0, whose tasks have all completed, from pool
 INFO - Job 0 finished: count at SimpleApp.scala:14, took 0.317205 s
 INFO - Starting job: count at SimpleApp.scala:15
 INFO - Got job 1 (count at SimpleApp.scala:15) with 2 output partitions (allowLocal=false)
 INFO - Final stage: Stage 1(count at SimpleApp.scala:15)
 INFO - Parents of final stage: List()
 INFO - Missing parents: List()
 INFO - Submitting Stage 1 (FilteredRDD[3] at filter at SimpleApp.scala:15), which has no missing parents
 INFO - ensureFreeSpace(2768) called with curMem=188823, maxMem=1497964216
 INFO - Block broadcast_2 stored as values in memory (estimated size 2.7 KB, free 1428.4 MB)
 INFO - Removing broadcast 1
 INFO - ensureFreeSpace(1732) called with curMem=191591, maxMem=1497964216
 INFO - Block broadcast_2_piece0 stored as bytes in memory (estimated size 1732.0 B, free 1428.4 MB)
 INFO - Added broadcast_2_piece0 in memory on localhost:54016 (size: 1732.0 B, free: 1428.6 MB)
 INFO - Updated info of block broadcast_2_piece0
 INFO - Created broadcast 2 from broadcast at DAGScheduler.scala:838
 INFO - Submitting 2 missing tasks from Stage 1 (FilteredRDD[3] at filter at SimpleApp.scala:15)
 INFO - Adding task set 1.0 with 2 tasks
 INFO - Removing block broadcast_1
 INFO - Block broadcast_1 of size 2768 dropped from memory (free 1497773661)
 INFO - Removing block broadcast_1_piece0
 INFO - Block broadcast_1_piece0 of size 1732 dropped from memory (free 1497775393)
 INFO - Removed broadcast_1_piece0 on localhost:54016 in memory (size: 1732.0 B, free: 1428.6 MB)
 INFO - Updated info of block broadcast_1_piece0
 INFO - Starting task 0.0 in stage 1.0 (TID 2, localhost, PROCESS_LOCAL, 1323 bytes)
 INFO - Running task 0.0 in stage 1.0 (TID 2)
 INFO - Cleaned broadcast 1
 INFO - Found block rdd_1_0 locally
 INFO - Finished task 0.0 in stage 1.0 (TID 2). 1757 bytes result sent to driver
 INFO - Starting task 1.0 in stage 1.0 (TID 3, localhost, PROCESS_LOCAL, 1323 bytes)
 INFO - Running task 1.0 in stage 1.0 (TID 3)
 INFO - Finished task 0.0 in stage 1.0 (TID 2) in 16 ms on localhost (1/2)
 INFO - Found block rdd_1_1 locally
 INFO - Finished task 1.0 in stage 1.0 (TID 3). 1757 bytes result sent to driver
 INFO - Finished task 1.0 in stage 1.0 (TID 3) in 6 ms on localhost (2/2)
 INFO - Stage 1 (count at SimpleApp.scala:15) finished in 0.023 s
 INFO - Removed TaskSet 1.0, whose tasks have all completed, from pool
 INFO - Job 1 finished: count at SimpleApp.scala:15, took 0.058930 s
 Lines with a: 8, Lines with b: 6
 */
object SimpleApp {

  def main(args: Array[String]) {
    val logFile = "file:///home/hadoop/soft/cdh5.2.0/spark-1.1.0-cdh5.2.0/README"
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
    val sc = new SparkContext(conf)

    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))  // Lines with a: 8, Lines with b: 6
  }

}
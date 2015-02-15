package com.zqh.spark

import org.apache.spark.graphx.{Graph, Edge}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

object AmpCamp5 {

  val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    //pagecounts
    //tachyon
    sparkSQL
  }

  // http://ampcamp.berkeley.edu/5/exercises/data-exploration-using-spark.html
  def pagecounts {
    // 1. Warm up by creating an RDD named pagecounts from the input files.
    val pagecounts = sc.textFile("file:/home/hadoop/data/pagecounts")

    // 2. Take a peek at the data, use the take operation of an RDD to get the first K records.
    println(pagecounts.take(10).mkString("\n"))
    pagecounts.take(10).foreach(println)
    /** 0              1    2               3  4
      * 20090505-000000 aa.b Wikibooks:About 1 15719
      * 20090505-000000 aa ?14mFX1ildVnBc 1 13205
      * 20090505-000000 aa ?53A%2FuYP3FfnKM 1 13207
      */

    // 3. how many records in total are in this data set
    pagecounts.count

    // 1.derive an RDD containing only English pages from pagecounts.
    // For each record, we can split it by the field delimiter and get the second field and then compare it with the string “en”.
    val enPages = pagecounts.filter(_.split(" ")(1) == "en").cache

    // 2. But since enPages was marked as “cached” in the previous step,
    enPages.count
    // if you run count on the same RDD again, it should return an order of magnitude faster.
    enPages.count

    // 3. Generate a histogram of total page views on Wikipedia English pages for the date range represented in our dataset
    // First, we generate a key value pair for each line; the key is the date, and the value is the number of pageviews for that date
    // Next, we shuffle the data and group all values of the same key together. Finally we sum up the values for each key.
    val enTuples = enPages.map(line => line.split(" "))
    val enKeyValuePairs = enTuples.map(line => (line(0).substring(0, 8), line(3).toInt))
    enKeyValuePairs.reduceByKey(_+_, 1).collect

    // all-in-one together
    enPages.map(line => line.split(" ")).map(line => (line(0).substring(0, 8), line(3).toInt)).reduceByKey(_+_, 1).collect

    // 4. find pages that were viewed more than 200,000 times
    enPages.map(l => l.split(" "))          // split each line of data into its respective fields.
      .map(l => (l(2), l(3).toInt))   // extract the fields for page name and number of page views.
      .reduceByKey(_+_, 40)           // reduce by key again, this time with 40 reducers.
      .filter(x => x._2 > 200000)     // filter out pages with less than 200,000 total views
      .map(x => (x._2, x._1))         // change position of pagename and #pageviews
      .collect.foreach(println)       // print the result
  }

  // http://ampcamp.berkeley.edu/5/exercises/tachyon.html
  def tachyon{
    // Because /LICENSE is in memory, when a new Spark program comes up, it can load in memory data directly from Tachyon
    sc.hadoopConfiguration.set("fs.tachyon.impl", "tachyon.hadoop.TFS")

    // Input/Output with Tachyon
    var file = sc.textFile("tachyon://localhost:19998/LICENSE")
    val counts = file.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey(_ + _)

    counts.saveAsTextFile("tachyon://localhost:19998/result")

    // Store RDD OFF_HEAP in Tachyon
    counts.persist(org.apache.spark.storage.StorageLevel.OFF_HEAP)
    counts.take(10)
    counts.take(10)
  }

  // http://ampcamp.berkeley.edu/5/exercises/data-exploration-using-spark-sql.html
  def sparkSQL {


  }

  // http://spark.apache.org/docs/latest/streaming-programming-guide.html
  def streamming {
    // Create a local StreamingContext with two working thread and batch interval of 1 second.
    // The master requires 2 cores to prevent from a starvation scenario.
    val ssc = SparkUtil.getStreamContext("local[2]", "NetworkWordCount")

  }

  // http://ampcamp.berkeley.edu/5/exercises/graph-analytics-with-graphx.html
  def graphX {
    val vertexArray = Array(
      (1L, ("Alice", 28)),
      (2L, ("Bob", 27)),
      (3L, ("Charlie", 65)),
      (4L, ("David", 42)),
      (5L, ("Ed", 55)),
      (6L, ("Fran", 50))
    )
    val edgeArray = Array(
      Edge(2L, 1L, 7),
      Edge(2L, 4L, 2),
      Edge(3L, 2L, 4),
      Edge(3L, 6L, 3),
      Edge(4L, 1L, 1),
      Edge(5L, 2L, 2),
      Edge(5L, 3L, 8),
      Edge(5L, 6L, 3)
    )

    val conf = new SparkConf().setMaster("local").setAppName("Hello GraphX")
    val sc = new SparkContext(conf)

    val vertexRDD: RDD[(Long, (String, Int))] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[Int]] = sc.parallelize(edgeArray)

    val graph: Graph[(String, Int), Int] = Graph(vertexRDD, edgeRDD)

    //David is 42
    //Fran is 50
    //Charlie is 65
    //Ed is 55
    // Solution 1
    graph.vertices.filter { case (id, (name, age)) => age > 30 }.collect.foreach {
      case (id, (name, age)) => println(s"$name is $age")
    }

    // Solution 2
    graph.vertices.filter(v => v._2._2 > 30).collect.foreach(v => println(s"${v._2._1} is ${v._2._2}"))

    // Solution 3
    for ((id,(name,age)) <- graph.vertices.filter { case (id,(name,age)) => age > 30 }.collect) {
      println(s"$name is $age")
    }

  }
}
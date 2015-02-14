package com.zqh.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

/**
 * No Need to start others, just run it as Scala Application on IDEA. you should setMaster, or else will get
 * Caused by: org.apache.spark.SparkException: A master URL must be set in your configuration at ...
 */
object SimpleApp {

  val conf = new SparkConf().setAppName("Simple Application").setMaster("local")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {
    //simpleapp
    //pagecounts
    //tachyon
    sparkSQL
  }

  // ** Simpel Application **
  def simpleapp{
    val logFile = "file:/home/hadoop/data/README.md"
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
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
  def sparkSQL{
    import org.apache.spark.sql.SQLContext

    val sqlContext = new SQLContext(sc)
    sqlContext.setConf("spark.sql.parquet.binaryAsString", "true")

  }

  def dataguru {
    dataguru_parallelize
    dataguru_kv
    dataguru_file
    dataguru_sogou
  }

  //parallelize演示
  def dataguru_parallelize{
    val num=sc.parallelize(1 to 10)
    val doublenum = num.map(_*2)
    val threenum = doublenum.filter(_ % 3 == 0)
    threenum.collect
    threenum.toDebugString

    val num1=sc.parallelize(1 to 10,6)
    val doublenum1 = num1.map(_*2)
    val threenum1 = doublenum1.filter(_ % 3 == 0)
    threenum1.collect
    threenum1.toDebugString

    threenum.cache()
    val fournum = threenum.map(x=>x*x)
    fournum.collect
    fournum.toDebugString
    threenum.unpersist()

    num.reduce (_ + _)
    num.take(5)
    num.first
    num.count
    num.take(5).foreach(println)
  }

  //K-V演示
  def dataguru_kv {
    val kv1=sc.parallelize(List(("A",1),("B",2),("C",3),("A",4),("B",5)))
    kv1.sortByKey().collect //注意sortByKey的小括号不能省
    kv1.groupByKey().collect
    kv1.reduceByKey(_+_).collect

    val kv2=sc.parallelize(List(("A",4),("A",4),("C",3),("A",4),("B",5)))
    kv2.distinct.collect
    kv1.union(kv2).collect

    val kv3=sc.parallelize(List(("A",10),("B",20),("D",30)))
    kv1.join(kv3).collect
    kv1.cogroup(kv3).collect

    val kv4=sc.parallelize(List(List(1,2),List(3,4)))
    kv4.flatMap(x=>x.map(_+1)).collect
  }

  //文件读取演示
  def dataguru_file {
    val rdd1 = sc.textFile("hdfs://hadoop1:8000/dataguru/week2/directory/")
    rdd1.toDebugString
    val words=rdd1.flatMap(_.split(" "))
    val wordscount=words.map(x=>(x,1)).reduceByKey(_+_)
    wordscount.collect
    wordscount.toDebugString

    val rdd2 = sc.textFile("hdfs://hadoop1:8000/dataguru/week2/directory/*.txt")
    rdd2.flatMap(_.split(" ")).map(x=>(x,1)).reduceByKey(_+_).collect

    //gzip压缩的文件
    val rdd3 = sc.textFile("hdfs://hadoop1:8000/dataguru/week2/test.txt.gz")
    rdd3.flatMap(_.split(" ")).map(x=>(x,1)).reduceByKey(_+_).collect
  }

  //日志处理演示
  //http://download.labs.sogou.com/dl/q.html 完整版(2GB)：gz格式
  //访问时间\t用户ID\t[查询词]\t该URL在返回结果中的排名\t用户点击的顺序号\t用户点击的URL
  //SogouQ1.txt、SogouQ2.txt、SogouQ3.txt分别是用head -n 或者tail -n 从SogouQ数据日志文件中截取
  def dataguru_sogou {
    //搜索结果排名第1，但是点击次序排在第2的数据有多少?
    val rdd1 = sc.textFile("hdfs://hadoop1:8000/dataguru/data/SogouQ1.txt")
    val rdd2=rdd1.map(_.split("\t")).filter(_.length==6)
    rdd2.count()
    val rdd3=rdd2.filter(_(3).toInt==1).filter(_(4).toInt==2)
    rdd3.count()
    rdd3.toDebugString

    //session查询次数排行榜
    val rdd4=rdd2.map(x=>(x(1),1)).reduceByKey(_+_).map(x=>(x._2,x._1)).sortByKey(false).map(x=>(x._2,x._1))
    rdd4.toDebugString
    rdd4.saveAsTextFile("hdfs://hadoop1:8000/dataguru/week2/output1")

    //cache()演示
    //检查block命令：bin/hdfs fsck /dataguru/data/SogouQ3.txt -files -blocks -locations
    val rdd5 = sc.textFile("hdfs://hadoop1:8000/dataguru/data/SogouQ3.txt")
    rdd5.cache()
    rdd5.count()
    rdd5.count()  //比较时间

    //join演示
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
    case class Register (d: java.util.Date, uuid: String, cust_id: String, lat: Float,lng: Float)
    case class Click (d: java.util.Date, uuid: String, landing_page: Int)
    val reg = sc.textFile("hdfs://hadoop1:8000/dataguru/week2/join/reg.tsv").map(_.split("\t")).map(r => (r(1), Register(format.parse(r(0)), r(1), r(2), r(3).toFloat, r(4).toFloat)))
    val clk = sc.textFile("hdfs://hadoop1:8000/dataguru/week2/join/clk.tsv").map(_.split("\t")).map(c => (c(1), Click(format.parse(c(0)), c(1), c(2).trim.toInt)))
    reg.join(clk).take(2)
  }
}
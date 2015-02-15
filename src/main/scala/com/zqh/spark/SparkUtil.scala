package com.zqh.spark

import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.streaming._

/**
 * Created by hadoop on 15-2-15.
 */
object SparkUtil {

  def getSparkContext(appName : String = "SimpleApp", master : String = "local") {
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val sc = new SparkContext(conf)
  }

  def getSQLContext(appName : String = "SimpleApp", master : String = "local") {
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)
    sqlContext.setConf("spark.sql.parquet.binaryAsString", "true")
  }

  def getStreamContext(appName : String = "SimpleApp", master : String = "local"){
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val ssc = new StreamingContext(conf, Seconds(1))
  }

}

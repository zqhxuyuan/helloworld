package com.zqh.scala.pis

/**
 * Created by zqhxuyuan on 15-2-28.
 */
object ch08_closure {

  import scala.io.Source
  object LongLines {
    def processFile(filename: String, width: Int) {
      val source = Source.fromFile(filename)
      for (line <- source.getLines)
        processLine(filename, width, line)
    }
    private def processLine(filename:String, width:Int, line:String) {
      if (line.length > width)
        println(filename+": "+line.trim)
    }
  }
  
}

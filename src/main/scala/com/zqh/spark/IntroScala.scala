package com.zqh.spark

/**
 * Created by hadoop on 15-2-14.
 *
 * http://ampcamp.berkeley.edu/5/exercises/introduction-to-the-scala-shell.html
 */
object IntroScala extends App{

  // 2. Declare a list of integers as a variable
  val myNumbers = List(1, 2, 5, 4, 7, 3)

  // 3. Declare a function, cube, that computes the cube (third power) of an Int.
  def cube(a: Int): Int = a * a * a

  // 4. Apply the function to myNumbers using the map function.
  // x是List中的每个元素,对每个x都将参数传给cube,最后每个新计算出来的结果作为新的List的每个返回值.
  myNumbers.map(x => cube(x))

  println(myNumbers) // myNumber没有发生变化! List(1, 2, 5, 4, 7, 3)

  // 5. writing the function inline in a map call, using closure notation.
  println(myNumbers.map{x => x * x * x})  // 发生了变化! List(1, 8, 125, 64, 343, 27)

  // 6. Define a factorial function that computes n! = 1 * 2 * … * n given input n
  // Then compute the sum of factorials in myNumbers.
  def factorial(n:Int):Int = if (n==0) 1 else n * factorial(n-1)

  // 函数作为参数,myNumbers的每个元素作为map函数里的factorial函数的参数,由于只有一个参数,可以省略
  myNumbers.map(factorial).sum
  myNumbers.map(factorial _).sum
  myNumbers.map(x => factorial(x)).sum

  // 7. Do a wordcount of a textfile.
  // create and populate a Map with words as keys and counts of the number of occurrences of the word as values.
  import scala.io.Source
  import collection.mutable.HashMap

  // load a text file as an array of lines
  // lines是一个数组:Array[String],数组的每个元素是文件的每一行
  val lines = Source.fromFile("/home/hadoop/data/README.md").getLines.toArray
  // scala访问数组的方式是(),而不是[]
  println(lines(0))

  // instantiate a collection.mutable.HashMap[String,Int] and use functional methods to populate it with wordcounts.
  // counts是一个HashMap, 其中Map[k,v]的默认v值为0. 即当第一次出现key时,默认它对应的value=0
  val counts = new HashMap[String, Int].withDefaultValue(0)
  println(counts("no-key"))  // It's 0

  // flatMap, 先flat,再Map? 还是先map,再flat? Offcourse first flat, then map!
  // lines=Array[String],其中数组的元素是每一行的内容 ==> 对应了下面的line参数
  // 经过flat后会将所有的行组成一个字符串. 即整个文件的内容都包含在一个字符串里面
  // flatMap接受一个map参数:字符串的split会返回一个字符串数组:String[] arr = String.split(" ")
  // 所以最后的返回值是文件的每一个单词:Array[String],其中数组的每个元素是每个单词
  // 上一步的Array[String]是单词数组,其中每个单词会作为foreach的参数word
  // 由于counts是一个Map,所以Map(key)=value
  lines.flatMap(line => line.split(" ")).foreach(word => counts(word) += 1)

  // just a test
  var ln=0
  lines.flatMap(line => {
    ln = ln + 1
    if(ln < 10){
      println("LINE"+ln + ":" + line)
    }
    line.split(" ")
  })

  println(counts.take(10).mkString("\n"))
  println("------------------------------")

  // Or a purely functional solution:
  val emptyCounts = Map[String,Int]().withDefaultValue(0)

  val words = lines.flatMap(line => line.split(" "))

  val counts2 = words.foldLeft(emptyCounts)({
    (currentCounts: Map[String,Int], word: String) => currentCounts.updated(word, currentCounts(word) + 1)
  })

  println(counts2.take(10).mkString("\n"))

}

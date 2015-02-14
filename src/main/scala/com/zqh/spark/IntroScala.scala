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
  myNumbers.map(x => cube(x))

  // 5. writing the function inline in a map call, using closure notation.
  myNumbers.map{x => x * x * x}

  // 6. Define a factorial function that computes n! = 1 * 2 * … * n given input n
  // Then compute the sum of factorials in myNumbers.
  def factorial(n:Int):Int = if (n==0) 1 else n * factorial(n-1)

  myNumbers.map(factorial).sum

  
  // 7. Do a wordcount of a textfile.
  // create and populate a Map with words as keys and counts of the number of occurrences of the word as values.
  import scala.io.Source
  import collection.mutable.HashMap

  // load a text file as an array of lines
  val lines = Source.fromFile("/home/hadoop/data/README.md").getLines.toArray

  // instantiate a collection.mutable.HashMap[String,Int] and use functional methods to populate it with wordcounts.
  val counts = new HashMap[String, Int].withDefaultValue(0)

  lines.flatMap(line => line.split(" ")).foreach(word => counts(word) += 1)

  println(counts.take(10).mkString("\n"))
  println("------------------------------")

  // Or a purely functional solution:
  val emptyCounts = Map[String,Int]().withDefaultValue(0)

  val words = lines.flatMap(line => line.split(" "))

  val counts2 = words.foldLeft(emptyCounts)({(currentCounts: Map[String,Int], word: String) => currentCounts.updated(word, currentCounts(word) + 1)})

  println(counts2.take(10).mkString("\n"))

}

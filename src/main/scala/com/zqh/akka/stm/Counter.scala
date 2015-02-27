package com.zqh.akka.stm

import akka.pattern.ask
import akka.actor._
import akka.transactor._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.stm._

/**
 * Created by hadoop on 15-2-26.
 *
 * http://www.gtan.com/akka_doc/scala/transactors.html
 */
object Counter extends App{

  //-----------------------------
  val system = ActorSystem("app")

  val counter1 = system.actorOf(Props[Counter], name = "counter1")
  val counter2 = system.actorOf(Props[Counter], name = "counter2")

  counter1 ! Coordinated(Increment(Some(counter2)))
  implicit val timeout = Timeout(10 seconds)

  val count = Await.result(counter1 ? GetCount, timeout.duration)
  println(count)

  //---------------------------------------------------
  case class Increment(friend: Option[ActorRef] = None)
  case object GetCount

  class Counter extends Actor {
    val count = Ref(0)

    def receive = {
      case coordinated @ Coordinated(Increment(friend)) => {
        friend foreach (_ ! coordinated(Increment()))
        coordinated atomic { implicit t =>
          count transform (_ + 1)
        }
      }
      case GetCount ⇒ sender ! count.single.get
    }
  }

}

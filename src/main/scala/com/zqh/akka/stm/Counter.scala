package com.zqh.akka.stm

/**
 * Created by hadoop on 15-2-26.
 */
object Counter extends App{

  import akka.actor._
  import akka.transactor._
  import scala.concurrent.stm._

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

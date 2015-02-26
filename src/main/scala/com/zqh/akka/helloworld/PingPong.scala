package com.zqh.akka.helloworld

import akka.actor.{Props, ActorSystem, Actor}

object PingPong extends App{
  val system = ActorSystem("MyActorSystem")
  val myActor = system.actorOf(Props[PingPongActor], name = "myActor")
  myActor ! PING

  case class PING()
  case class PONG()

  class PingPongActor extends Actor {
    import context._

    var count = 0

    def receive: Receive = {
      case PING =>
        println("PING")
        count = count + 1
        Thread.sleep(100)
        self ! PONG
        become {  //切换到pong的逻辑，ping的逻辑会暂时放在stack里面
          case PONG =>
            println("PONG")
            count = count + 1
            Thread.sleep(100)
            self ! PING
            unbecome() //恢复ping的逻辑
        }
        if(count > 10) context.stop(self)
    }
  }
}

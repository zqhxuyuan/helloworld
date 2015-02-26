package com.zqh.akka.helloworld

import akka.actor._

import scala.collection.immutable.HashMap

/**
 * Created by hadoop on 15-2-26.
 */
object Monitor extends App{
  val system = ActorSystem("MyActorSystem")
  val monitor = system.actorOf(Props[MonitorActor], name = "monitor")
  val worker = system.actorOf(Props[WorkerActor], name = "worker")
  worker ! "DEAD"


  case class Result()
  case class DeadWorker()
  case class RegisterWorker(val worker: ActorRef, val supervisor: ActorRef)

  class WorkerActor extends Actor with ActorLogging {

    var state: Int = 0
    override def preStart() {
      log.info("Starting WorkerActor instance hashcode # {}", this.hashCode())
    }
    override def postStop() {
      log.info("Stopping WorkerActor instance hashcode # {}", this.hashCode());
    }
    def receive: Receive = {
      case value: Int =>
        state = value
      case result: Result =>
        sender ! state
      case _ =>
        context.stop(self) //worker会terminate
    }
  }

  //定义MonitorActor
  class MonitorActor extends Actor with ActorLogging {
    var monitoredActors = new HashMap[ActorRef, ActorRef]
    def receive: Receive = {
      case t: Terminated => //收到actor发来的terminated msg
        if (monitoredActors.contains(t.actor)) {
          log.info("Received Worker Actor Termination Message -> " + t.actor.path)
          log.info("Sending message to Supervisor")
          val value: Option[ActorRef] = monitoredActors.get(t.actor)
          value.get ! new DeadWorker() //通知注册的supervisor
        }
      case msg: RegisterWorker => //注册
        context.watch(msg.worker) //关键一步,调用context.watch来监控这个worker actor, 这样worker会在terminated时, 发送msg给monitor
        monitoredActors += msg.worker -> msg.supervisor //将actor和想监控他的supervisor放入map
    }
  }
}


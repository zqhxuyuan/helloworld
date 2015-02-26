package com.zqh.akka.helloworld

import akka.actor._
import akka.actor.SupervisorStrategy.{Escalate, Stop, Restart, Resume}
import akka.actor.{OneForOneStrategy, ActorLogging, Actor}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await

/**
 * Created by hadoop on 15-2-26.
 *
 * Error:(22, 28) value ? is not a member of akka.actor.ActorRef
  var future = (supervisor ? new Result).mapTo[Int]
                           ^
 * 解决办法:https://groups.google.com/forum/#!topic/play-framework/a8Lh3v7jAZw
 *
 * http://www.cnblogs.com/fxjwind/p/3428258.html
 */
object SuperWorker extends App{
  val system = ActorSystem("faultTolerance")
  val log = system.log
  val originalValue: Int = 0
  val supervisor = system.actorOf(Props[SupervisorActor], name = "supervisor")
  log.info("Sending value 8, no exceptions should be thrown! ")
  var mesg: Int = 8
  supervisor ! mesg
  implicit val timeout = Timeout(5)
  var future = (supervisor ? new Result).mapTo[Int]
  var result = Await.result(future, timeout.duration)
  log.info("Value Received-> {}", result)


  //Work Actor的定义,提高对state的更改和查询
  case class Result()

  class WorkerActor extends Actor with ActorLogging {
    var state: Int = 0 //actor需要维护的状态

    override def preStart() {
      log.info("Starting WorkerActor instance hashcode # {}", this.hashCode())
    }
    override def postStop() {
      log.info("Stopping WorkerActor instance hashcode # {}", this.hashCode())
    }

    //对于正常的msg外,会抛出各种异常
    def receive: Receive = {
      case value: Int =>
        if (value <= 0) //小于等于0的时候,抛出ArithmeticException
          throw new ArithmeticException("Number equal or less than zero")
        else
          state = value
      case result: Result => //对于结果查询,返回结果
        sender ! state
      case ex: NullPointerException =>
        throw new NullPointerException("Null Value Passed")
      case _ => //其他的输入抛出IllegalArgumentException
        throw new IllegalArgumentException("Wrong Argument")
    }
  }

  //Supervisor Actor的定义, 负责创建Work Actor和supervisorStrategy
  class SupervisorActor extends Actor with ActorLogging {
    val childActor = context.actorOf(Props[WorkerActor],name = "workerActor") //创建子Actor

    //创建supervisorStrategy是Supervisor的关键
    //前两个参数, maxNrOfRetries:重试的次数
    //the number of times an actor is allowed to be restarted before it is assumed to be dead.
    //A negative number implies no limits.
    //withinTimeRange:重试的间隔
    //关键是第三个参数, 定义了异常处理的策略
    //下面针对worker抛出的不同的异常, 匹配了不同的处理方法
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = Timeout(60).duration) {
      case _: ArithmeticException => Resume
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }

    //可以接收两种msg
    def receive = {
      case result: Result =>
        childActor.tell(result, sender)
      case msg: Object =>
        childActor ! msg
    }
  }
}

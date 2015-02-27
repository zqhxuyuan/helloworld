package com.zqh.akka.essentials.stm.transactor

import akka.actor.SupervisorStrategy.{Escalate, Stop, Resume}
import akka.actor._
import akka.transactor.{Coordinated, CoordinatedTransactionException}
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

/**
 * Created by hadoop on 15-2-27.
 */
object BankApplication {
  val system = ActorSystem("STM-Example")
  implicit val timeout = Timeout(5 seconds)
  val bank = system.actorOf(Props[BankActor], name = "BankActor")

  def main(args: Array[String]): Unit = {
    showBalances()
    bank ! new TransferMsg(1500)
    showBalances()
    bank ! new TransferMsg(1400)
    showBalances()
    bank ! new TransferMsg(3500)
    showBalances()

    system.shutdown()
  }

  def showBalances(): Unit = {
    Thread.sleep(2000)
    bank ! new AccountBalance("XYZ", 0)
    bank ! new AccountBalance("ABC", 0)
  }

  case class AccountBalance(accountNumber: String, accountBalance: Float)
  case class AccountCredit(amount: Float)
  case class AccountDebit(amount: Float)
  case class TransferMsg(amtToBeTransferred: Float)


  class BankActor extends Actor with ActorLogging {

    val transferActor = context.actorOf(Props[TransferActor], name = "TransferActor")
    implicit val timeout = Timeout(5 seconds)

    def receive = {
      case transfer: TransferMsg =>
        transferActor ! transfer
      case balance: AccountBalance =>
        val future = ask(transferActor, balance).mapTo[AccountBalance]
        val account = Await.result(future, timeout.duration)
        log.info("Account #{} , Balance {}", account.accountNumber,
          account.accountBalance)
    }

    override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: CoordinatedTransactionException => Resume
      case _: IllegalStateException => Resume
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }
  }

  class TransferActor extends Actor {

    val fromAccount = "XYZ";
    val toAccount = "ABC";

    val from = context.actorOf(Props(new AccountActor(fromAccount, 5000)), name = fromAccount)
    val to = context.actorOf(Props(new AccountActor(toAccount, 1000)), name = toAccount)
    implicit val timeout = Timeout(5 seconds)

    override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10 seconds) {
      case _: CoordinatedTransactionException => Resume
      case _: IllegalStateException => Resume
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }

    def receive: Receive = {
      case message: TransferMsg =>
        val coordinated = Coordinated()
        coordinated atomic {
          implicit t =>
            to ! coordinated(new AccountCredit(
              message.amtToBeTransferred))
            from ! coordinated(new AccountDebit(
              message.amtToBeTransferred))
        }
      case message: AccountBalance =>
        if (message.accountNumber.equalsIgnoreCase(fromAccount)) {
          from.tell(message, sender)
        } else if (message.accountNumber.equalsIgnoreCase(toAccount)) {
          to.tell(message, sender)
        }
    }
  }

  class AccountActor(accountNumber: String, inBalance: Float) extends Actor {

    val balance = Ref(inBalance)

    def receive = {
      case value: AccountBalance =>
        sender ! new AccountBalance(accountNumber, balance.single.get)

      case coordinated@Coordinated(message: AccountDebit) =>
        // coordinated atomic ...
        coordinated atomic {
          implicit t =>
            //check for funds availability
            if (balance.get(t) > message.amount)
              balance.transform(_ - message.amount)
            else
              throw new IllegalStateException(
                "Insufficient Balance")
        }
      case coordinated@Coordinated(message: AccountCredit) =>
        // coordinated atomic ...
        coordinated atomic {
          implicit t =>
            balance.transform(_ + message.amount)
        }
    }
  }
}

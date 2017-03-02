package com.swarmbits.bf

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestFSMRef, TestKit, TestProbe}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class BFInterpreterTest extends TestKit(
  ActorSystem("BFInterpreter", ConfigFactory.parseString(TestUtils.config)))
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val helloWorldProgram = "++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.".toCharArray

  override def afterAll {
    shutdown()
  }

  "The BFInterpreter when executing the helloWorldProgram " should {
    "say Hello World!" in {

      within(2000 millis) {
        val writer = TestActorRef(new OutputHolder)
        val fsm = TestFSMRef(
          new BFInterpreter("testprogram", helloWorldProgram, new Array[Int](1000), new mutable.ArrayStack[Int](), writer))
        val probe = TestProbe()
        probe watch fsm
        fsm ! Start
        probe.expectTerminated(fsm) // Deadwatch fsm as it should terminate
        implicit val timeout = Timeout(1 seconds)
        val future = writer ? ToString
        val result = Await.result(future, 100 milliseconds).asInstanceOf[String]
        assert(result === "Hello World!\n")
      }

    }

  }

}

object TestUtils {
  val config =
    """
    akka {
      loglevel = "INFO"
    }
    """
}

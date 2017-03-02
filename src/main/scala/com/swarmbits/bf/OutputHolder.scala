package com.swarmbits.bf

import java.io.CharArrayWriter
import java.nio.CharBuffer

import akka.actor.Actor
import akka.event.Logging

class OutputHolder extends Actor {

  val log = Logging(context.system, this)

  val buffer = new CharArrayWriter();

  def receive = {
    case Write(c) => {
      buffer.write(c)
      log.info("Char added {}", c.toInt)
    }
    case ToString => {
      log.info("{}", buffer.size())
      sender ! buffer.toString
    }
  }

}

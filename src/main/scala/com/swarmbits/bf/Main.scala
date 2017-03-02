package com.swarmbits.bf

import akka.actor.{ActorSystem, Props}

import scala.collection.mutable

object Main extends App {

  val program = args(0).toCharArray
  println("Brainfuck input program: " + args(0))

  val system = ActorSystem("BrainFuck")

  val writer = system.actorOf(Props(new OutputHolder))
  system.actorOf(Props(new BFInterpreter("testprogram", program, new Array[Int](1000), new mutable.ArrayStack[Int](), writer))) ! Start
  Thread.sleep(200)
  writer ! ToString

  system.terminate()

}

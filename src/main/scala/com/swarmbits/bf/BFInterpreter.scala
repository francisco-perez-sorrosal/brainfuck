package com.swarmbits.bf

import akka.actor.FSM.Normal
import akka.actor.{Actor, ActorRef, ActorSystem, LoggingFSM}
import akka.testkit.TestLatch

import scala.collection.mutable

sealed trait State
case object WaitForInstructions extends State
case object Processing extends State
case object Stopped extends State

case class StateData(ip : Int, mp : Int)

class BFInterpreter(programName : String,
                    byteCode : Array[Char],
                    memory : Array[Int],
                    stack : mutable.ArrayStack[Int],
                    outputWriter: ActorRef) extends Actor with LoggingFSM[State, StateData] {

  startWith(WaitForInstructions, new StateData(0, 0))

  when(WaitForInstructions) {
    case Event(Start, data: StateData) => {
      log.info("Starting bytecode processing of program " + programName)
      goto(Processing)
    }
  }

  when(Processing) {
    case Event(Instruction, data: StateData) => {
      if (data.ip >= byteCode.length) {
        stop
      } else {
        val instruction: Char = byteCode(data.ip)
        log.debug("Instruction {}", instruction)

        instruction match {
          case '+' => {
            memory(data.mp) += 1
            val newStateData = data.copy(ip = data.ip + 1)
            goto(Processing) using newStateData

          }
          case '-' => {
            memory(data.mp) -= 1
            val newStateData = data.copy(ip = data.ip + 1)
            goto(Processing) using newStateData

          }
          case '>' => {
            val newStateData = StateData(data.ip + 1, data.mp + 1)
            goto(Processing) using newStateData
          }
          case '<' => {
            val newStateData = StateData(data.ip + 1, data.mp - 1)
            goto(Processing) using newStateData
          }
          case '.' => {
            outputWriter ! Write(memory(data.mp).toChar)
            val newStateData = data.copy(ip = data.ip + 1)
            goto(Processing) using newStateData
          }
          case '[' => {
            stack.push(data.ip)
            val newStateData = data.copy(ip = data.ip + 1)
            goto(Processing) using newStateData
          }
          case ']' => {
            val cond = memory(data.mp)
            if (cond == 0) {
              stack.pop()
              val newStateData = data.copy(ip = data.ip + 1)
              goto(Processing) using newStateData
            } else {
              val jump = stack.top
              val newStateData = data.copy(ip = jump + 1)
              goto(Processing) using newStateData
            }
          }
          case _ =>
            log.error("Unknown instruction {}", instruction)
            val newStateData = data.copy(ip = data.ip + 1)
            goto(Processing) using newStateData
        }
      }
    }
  }

  when(Stopped) {
    case Event(_, data: StateData) => {
      stop
    }
  }

  onTransition {

    case WaitForInstructions -> Processing => {
      self ! Instruction
    }

    case Processing -> Processing => {
      self ! Instruction
    }

  }

  onTermination {
    case StopEvent(Normal, state, data) => {
      log.info("Stopping Processing FSM")
    }
  }

  initialize()

}

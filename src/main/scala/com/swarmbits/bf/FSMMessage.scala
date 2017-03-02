package com.swarmbits.bf

sealed trait FSMMessage
case object Start extends FSMMessage
case object Instruction extends FSMMessage
case object Stop extends FSMMessage

sealed trait OutputHolderMessage
case class Write(char : Char) extends OutputHolderMessage
case object ToString extends OutputHolderMessage



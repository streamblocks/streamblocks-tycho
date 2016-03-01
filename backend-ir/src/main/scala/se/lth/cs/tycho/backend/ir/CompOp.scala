package se.lth.cs.tycho.backend.ir

sealed trait CompOp {}
case object EQ extends CompOp
case object NE extends CompOp
case object GT extends CompOp
case object GE extends CompOp
case object LT extends CompOp
case object LE extends CompOp
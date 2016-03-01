package se.lth.cs.tycho.backend.ir

sealed trait LogicOp {}
case object OR extends LogicOp
case object AND extends LogicOp
case object XOR extends LogicOp
case object SHL extends LogicOp
case object SHR extends LogicOp
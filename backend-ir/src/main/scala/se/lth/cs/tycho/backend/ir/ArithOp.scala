package se.lth.cs.tycho.backend.ir

sealed trait ArithOp {}
case object ADD extends ArithOp
case object SUB extends ArithOp
case object MUL extends ArithOp
case object DIV extends ArithOp
case object REM extends ArithOp

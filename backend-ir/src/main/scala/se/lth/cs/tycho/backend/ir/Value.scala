package se.lth.cs.tycho.backend.ir

sealed trait Value
case class Variable(name: String) extends Value
case class IntegerValue(value: BigInt) extends Value
case class BooleanValue(value: Boolean) extends Value
case class ArrayValue(values: Seq[Value]) extends Value
case class StructureValue(values: Seq[Value]) extends Value
case class UnionValue(variant: Int, value: Value) extends Value

package se.lth.cs.tycho.backend.ir

sealed trait Type

case class IntegerType(size: Int, signed: Boolean) extends Type
case class UnitType() extends Type
case class FunctionType(parameterTypes: Seq[Type], returnType: Type) extends Type
case class ArrayType(elementType: Type, size: Option[Int]) extends Type
case class ReferenceType(referredType: Type) extends Type
case class StructureType(elementTypes: Seq[Type]) extends Type
case class UnionType(variantTypes: Seq[Type]) extends Type
case class NominalType(name: String) extends Type
case class ChannelType(tokeType: Type, size: Int) extends Type


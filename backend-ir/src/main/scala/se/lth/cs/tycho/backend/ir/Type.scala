package se.lth.cs.tycho.backend.ir

sealed trait Type

case class IntegerType(size: Int, signed: Boolean) extends Type
case object UnitType extends Type
case class FunctionType(parameterTypes: Seq[Type], returnType: Type) extends Type
case class ArrayType[T <: Type](elementType: T, size: Option[Long]) extends Type
case class ReferenceType[T <: Type](referredType: T) extends Type
case class StructureType(elementTypes: Seq[Type]) extends Type
case class UnionType(variantTypes: Seq[Type]) extends Type
case class NominalType(name: String) extends Type


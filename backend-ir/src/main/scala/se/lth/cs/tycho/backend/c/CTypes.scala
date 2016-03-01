package se.lth.cs.tycho.backend.c

import se.lth.cs.tycho.backend.ir._

import scala.annotation.tailrec

object CTypes {

  @tailrec
  def declaration(name: String, typ: Type): String = typ match {
    case IntegerType(size, true) => s"int${size}_t $name"
    case IntegerType(size, false) => s"uint${size}_t $name"
    case ReferenceType(referred) => declaration(s"(*$name)", referred)
    case ArrayType(element, Some(size)) => declaration(s"($name[$size])", element)
    case ArrayType(element, None) => declaration(s"($name[])", element)
    case FunctionType(params, ret) => declaration(s"$name${parameterList(params)}", ret)
    case UnitType => "void"
    case NominalType(typename) => s"$typename $name"
  }

  private def parameterList(types: Seq[Type]): String =
    types.zipWithIndex.map{ case (t, i) => declaration(s"p_$i", t) }.mkString("(", ", ", ")")

}

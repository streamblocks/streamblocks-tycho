package se.lth.cs.tycho.backend.typecheck

import se.lth.cs.tycho.backend.ir._

object TypeCheck {

  def checkUnit(unit: CodeUnit): Seq[String] = {
    nameAnalysis(unit)
  }

  private def nameAnalysis(unit: CodeUnit): Seq[String] =
    duplicateNames[FunctionDefinition](unit.functions, _.name) ++
      duplicateNames[VariableDefinition](unit.variables, _.name) ++
      duplicateNames[TypeDefinition](unit.types, _.name) ++
      functionNames(unit.functions)

  private def functionNames(functions: Seq[FunctionDefinition]): Seq[String] = {
    val names = functions.map(_.name).to[Set]
    functions.flatMap(_.body).collect {
      case Call(_,_,f,_) if !names(f) => s"Unknown funciton $f"
      case GetFunctionReference(_, _, f) if !names(f) => s"Unknown function $f"
    }
  }

  private def duplicateNames[A](definitions: Seq[A], name: A => String): Seq[String] =
    definitions
      .groupBy(name)
      .values
      .to[Seq]
      .filter(_.size > 1)
      .map(vars => s"Conflicting definitions: $vars")

  implicit class TypeOps(val t: Type) extends AnyVal {
    def structuralType(implicit tc: TypeContext): Type = tc.structuralType(t)
    def isReference(implicit tc: TypeContext): Boolean = tc.isReference(t)
  }

}

trait TypeContext {
  def structuralType(t: Type): Type
  def isReference(t: Type): Boolean = t match {
    case ReferenceType(_) => true
    case NominalType(name) => isReference(structuralType(t))
    case _ => false
  }
}

object TypeContext {
  def forUnit(unit: CodeUnit): TypeContext = new CodeUnitTypeContext(unit)
}

class CodeUnitTypeContext(unit: CodeUnit) extends TypeContext {
  def structuralType(t: Type): Type = {
    val structural = computeStructuralType(t)
    if (t == structural) t else structural
  }

  private def computeStructuralType(t: Type): Type = t match {
    case IntegerType(_,_) => t
    case UnitType => t
    case FunctionType(param, ret) => FunctionType(param.map(structuralType), structuralType(ret))
    case ArrayType(elem, size) => ArrayType(structuralType(elem), size)
    case ReferenceType(ref) => ReferenceType(structuralType(ref))
    case StructureType(elem) => StructureType(elem.map(structuralType))
    case UnionType(vari) => UnionType(vari.map(structuralType))
    case NominalType(name) => structuralType(unit.types.find(typedef => typedef.name == name).get.definedType)
  }
}

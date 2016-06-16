package se.lth.cs.tycho.backend.ir

/**
  * Defines a function with a name, return type, parameters and a body.
  *
  * @param name       the identifier of the function
  * @param returnType the return type
  * @param parameters the formal parameters
  * @param body       the sequence of instructions that computes the function
  */
case class FunctionDefinition(name: String,
                              returnType: Type,
                              parameters: Seq[VariableDefinition],
                              variables: Seq[VariableDefinition],
                              body: Seq[Instruction]) extends Attributable

case class ActorDefinition(name: String,
                           parameters: Seq[VariableDefinition],
                           state: Type,
                           variables: Seq[VariableDefinition],
                           body: Seq[ActorInstruction]) extends Attributable

case class VariableDefinition(name: String, variableType: Type, value: Option[Value] = None) extends Attributable

case class TypeDefinition(name: String, definedType: Type) extends Attributable

case class CodeUnit(functions: Seq[FunctionDefinition] = Seq.empty,
                    actors: Seq[ActorDefinition] = Seq.empty,
                    variables: Seq[VariableDefinition] = Seq.empty,
                    types: Seq[TypeDefinition] = Seq.empty) extends Attributable


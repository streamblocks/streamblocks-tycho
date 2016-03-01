package se.lth.cs.tycho.backend.ir

/**
  * Defines a possibly resumable function with a name, return type, state, parameters, variables and a body. Resumable
  * functions can yield to its caller and when called again, it resumes where it was before yielding. Functions
  * are either stateful or stateless, depending on if the list of state-variables is non-empty. Stateless functions
  * can be directly called but stateful functions can only be called through a function object that encapsulates its
  * state.
  *
  * @param name       the identifier of the function
  * @param returnType the return type
  * @param capture    the state of a function object
  * @param parameters the formal parameters
  * @param variables  the variables used in the function
  * @param body       the sequence of instructions that computes the function
  */
case class FunctionDefinition(name: String,
                              returnType: Type,
                              capture: Seq[VariableDefinition] = Seq.empty,
                              parameters: Seq[VariableDefinition],
                              variables: Seq[VariableDefinition],
                              body: Seq[Instruction]) extends Attributable


case class VariableDefinition(name: String, variableType: Type, value: Option[Value] = None)

case class TypeDefinition(name: String, definedType: Type)

case class CodeUnit(functions: Seq[FunctionDefinition] = Seq.empty,
                    variables: Seq[VariableDefinition] = Seq.empty,
                    types: Seq[TypeDefinition] = Seq.empty)


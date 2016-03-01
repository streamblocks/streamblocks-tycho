package se.lth.cs.tycho.backend.ir

sealed trait Instruction

trait Result {
  def typ: Type
  def result: String
}


// Function call
case class Call(result: String, typ: Type, function: String, arguments: Seq[String]) extends Instruction with Result
case class CallIndirect(result: String, typ: Type, reference: String, arguments: Seq[String]) extends Instruction with Result

// Value operations
case class Assign(result: String, typ: Type, value: Value) extends Instruction with Result

// Integer operations
case class Arithmetic(result: String, typ: Type, operation: ArithOp, x: String, y: String) extends Instruction with Result
case class Logic(result: String, typ: Type, operation: LogicOp, x: String, y: String) extends Instruction with Result
case class Comparison(result: String, typ: Type, operation: CompOp, x: String, y: String) extends Instruction with Result
case class Convert(result: String, typ: Type, value: String) extends Instruction with Result

// Reference operations
case class GetFunctionReference(result: String, typ: Type, function: String) extends Instruction with Result
case class CastReference(result: String, typ: Type, reference: String) extends Instruction with Result
case class GetReference(result: String, typ: Type, variable: String, path: Seq[Int]) extends Instruction with Result
case class GetReferredValue(result: String, typ: Type, reference: String) extends Instruction with Result
case class SetReferredValue(reference: String, value: String) extends Instruction

// Control flow operations
case class Label(name: String) extends Instruction
case class Branch(label: String) extends Instruction
case class BranchNotZero(cond: String, label: String) extends Instruction
case class Return(typ: Type, value: String) extends Instruction

// Filament
case class Yield(typ: Type, value: String) extends Instruction
case class CreateFilament(result: String, typ: Type, filament: String) extends Instruction
case class CreateThread(result: String, function: String) extends Instruction





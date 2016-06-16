package se.lth.cs.tycho.backend.ir

sealed trait Instruction

trait Result {
  def typ: Type
  def result: Variable
}

// Function call
case class Call(result: Variable, typ: Type, function: String, arguments: Seq[Value]) extends Instruction with Result

// Value operations
case class Assign(result: Variable, typ: Type, value: Value) extends Instruction with Result

// Integer operations
case class Arithmetic(result: Variable, typ: Type, operation: ArithOp, x: Value, y: Value) extends Instruction with Result
case class Logic     (result: Variable, typ: Type, operation: LogicOp, x: Value, y: Value) extends Instruction with Result
case class Comparison(result: Variable, typ: Type, operation: CompOp,  x: Value, y: Value) extends Instruction with Result

// Reference operations
case class GetReference(result: Variable, typ: Type, variable: Variable, path: Seq[Int]) extends Instruction with Result
case class ReadReference(result: Variable, typ: Type, reference: Variable) extends Instruction with Result
case class WriteReference(reference: Variable, value: Value) extends Instruction

// Control flow operations
case class Label(name: String) extends Instruction
case class Branch(label: String) extends Instruction
case class BranchNotZero(cond: Value, label: String) extends Instruction
case class Return(typ: Type, value: Value) extends Instruction

// Queue operations
case class QueuePeek(result: Variable, typ: Type, queue: Variable, offset: Value = IntegerValue(0)) extends Instruction with Result
case class QueueConsume(queue: Variable, length: Value) extends Instruction
case class QueueWrite(queue: Variable, tokenType: Type, value: Value) extends Instruction
case class QueueTestTokens(result: Variable, queue: Variable, length: Value) extends Instruction with Result { val typ = IntegerType(size=1, signed=false) }
case class QueueTestSpace(result: Variable, queue: Variable, length: Value) extends Instruction with Result { val typ = IntegerType(size=1, signed=false) }

sealed trait ActorInstruction extends Instruction

case class Wait(reason: Set[WaitReason]) extends ActorInstruction

sealed trait WaitReason
case class QueueFull(queue: Variable) extends WaitReason
case class QueueEmpty(queue: Variable) extends WaitReason

case class Yield() extends ActorInstruction

case class CreateActor(name: String, arguments: Seq[Value]) extends Instruction




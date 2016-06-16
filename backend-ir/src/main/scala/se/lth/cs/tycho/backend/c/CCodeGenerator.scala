package se.lth.cs.tycho.backend.c

import java.io.Writer

import se.lth.cs.tycho.backend.ir._

import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.Charset



object CCodeGenerator {
  def generate(file: Path, unit: CodeUnit): Unit = {
    generate(Files.newBufferedWriter(file, Charset.forName("UTF-8")), unit)
  }

  def generate(writer: Writer, unit: CodeUnit): Unit = {
    new Generator(new Emitter(writer), unit).generate()
  }

  private class Generator(emitter: Emitter, unit: CodeUnit) {
    import CTypes._
    import emitter._

    def generate(): Unit = {
      emit("#include <stdint.h>")
      newline()
      newline()

      unit.variables foreach emitVariableDeclataion

      unit.functions foreach emitFunctionDeclaration
      newline()

      unit.functions foreach emitFunctionDefinition
      newline()

      flush()
    }

    private def functionHeader(f: FunctionDefinition): String = {
      val params = f.parameters.map(v => declaration(v.name, v.variableType)).mkString(", ")
      modifiers(f) ++ declaration(s"${f.name}($params)", f.returnType)
    }

    private def modifiers(attr: Attributable): String = attr.attributes(CModifiers) match {
      case Some(mod) => mod.mkString("", " ", " ")
      case None => ""
    }

    private def emitFunctionDeclaration(f: FunctionDefinition): Unit = {
      emit(s"${functionHeader(f)};")
      newline()
    }

    private def emitFunctionDefinition(f: FunctionDefinition): Unit = {
      emit(s"${functionHeader(f)} {")
      indent()
      f.variables foreach emitVariableDeclataion
      f.body foreach emitInstruction
      deindent()
      emit("}")
      newline()
    }

    private def emitVariableDeclataion(definition: VariableDefinition): Unit = definition match {
      case VariableDefinition(name, typ, None) =>
        emit(s"${declaration(name, typ)};")
      case VariableDefinition(name, typ, Some(value)) =>
        emit(s"${declaration(name, typ)} = ${valueLiteral(value)};")
    }

    private def valueLiteral(value: Value): String = value match {
      case IntegerValue(x) => x.toString
      case Variable(name) => name
      case _ => ???
    }

    private def emitInstruction(i: Instruction): Unit = i match {
      case Return(_, v) => emit(s"return ${valueLiteral(v)};")
      case Label(label) => emit(s"$label:");
      case Branch(label) => emit(s"goto $label;");
      case BranchNotZero(cond, label) => emit(s"if (${valueLiteral(cond)}) goto $label;");
      case Arithmetic(Variable(name), _, op, x, y) => emit(s"$name = ${valueLiteral(x)} ${arithOp(op)} ${valueLiteral(y)};")
      case Logic(Variable(name), _, op, x, y) => emit(s"$name = ${valueLiteral(x)} ${logicOp(op)} ${valueLiteral(y)};")
      case Comparison(Variable(name), _, op, x, y) => emit(s"$name = ${valueLiteral(x)} ${compOp(op)} ${valueLiteral(y)};")
      case Call(Variable(name), _, func, args) => emit(s"""$name = $func(${args.map(valueLiteral).mkString(", ")});""")
      case _ => ???
    }

    private def compOp(op: CompOp) = op match {
      case EQ => "=="
      case NE => "!="
      case GT => ">"
      case GE => ">="
      case LT => "<"
      case LE => "<="
    }

    private def arithOp(op: ArithOp) = op match {
      case ADD => "+"
      case SUB => "-"
      case MUL => "*"
      case DIV => "/"
      case REM => "%"
    }

    private def logicOp(op: LogicOp) = op match {
      case AND => "&"
      case OR => "|"
      case XOR => "^"
      case SHL => "<<"
      case SHR => ">>"
    }
  }
}

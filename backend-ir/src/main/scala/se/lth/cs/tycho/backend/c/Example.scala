package se.lth.cs.tycho.backend.c

import java.io.PrintWriter

import se.lth.cs.tycho.backend.ir._

object Example {
  def main(args: Array[String]): Unit = {

    val i32 = IntegerType(32, signed = true)
    val add = FunctionDefinition(
      name = "add",
      returnType = i32,
      parameters = Seq(VariableDefinition("x", i32, None), VariableDefinition("y", i32, None)),
      variables = Seq(VariableDefinition("res", i32, None)),
      body = Seq(
        Arithmetic(Variable("res"), i32, ADD, Variable("x"), Variable("y")),
        Return(i32, Variable("res"))
      )
    )

    add.attributes(CModifiers) = Seq("static")

    CCodeGenerator.generate(new PrintWriter(System.out),
      CodeUnit(
        functions = Seq(add),
        variables = Seq(),
        types = Seq()
      )
    )

  }

}


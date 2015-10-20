package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.IntType;
import se.lth.cs.tycho.types.Type;
import se.lth.cs.tycho.types.UnitType;


import static org.multij.BindingKind.MODULE;

@Module
public interface Code {
	@Binding(MODULE)
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Types types() {
		return backend().types();
	}

	default Variables variables() {
		return backend().variables();
	}

	void assign(Type type, String lvalue, Expression expr);

	default void assign(IntType type, String lvalue, Expression expr) {
		assignScalar(type, lvalue, expr);
	}
	default void assign(UnitType type, String lvalue, Expression expr) { emitter().emit("%s = 0;", lvalue); }

	default void assignScalar(Type type, String lvalue, Expression expr) {
		emitter().emit("%s = %s;", lvalue, evaluate(expr));
	}

	default void assignScalar(Type type, String lvalue, ExprInput input) {
		assert !input.hasRepeat() : "Cannot assign a repeated input to a scalar.";
		Type portType = types().portType(input.getPort());
		String tmp = variables().generateTemp();
		emitter().emit("%s;", declaration(portType, tmp));
		emitter().emit("channel_peek(self->%s_channel, %d, sizeof(%s), (char*) &%s);", input.getPort().getName(), input.getOffset(), type(portType), tmp);
		emitter().emit("%s = %s;", lvalue, tmp); // should handle some discrepancies between port type and variable type.
	}

	String declaration(Type type, String name);

	default String declaration(IntType type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(UnitType type, String name) { return "char " + name; }

	String type(Type type);

	default String type(IntType type) {
		if (type.getSize().isPresent()) {
			return String.format("int%d_t", type.getSize().getAsInt());
		} else {
			return "int64_t";
		}
	}

	default String type(UnitType type) {
		return "char";
	}

	String evaluate(Expression expr);

	default String evaluate(ExprVariable variable) {
		return variables().name(variable.getVariable());
	}

	default String evaluate(ExprLiteral literal) {
		assert literal.getKind() == ExprLiteral.Kind.Integer ||
				literal.getKind() == ExprLiteral.Kind.True ||
				literal.getKind() == ExprLiteral.Kind.False;
		return literal.getText();
	}

	default String evaluate(ExprBinaryOp binaryOp) {
		assert binaryOp.getOperations().size() == 1 && binaryOp.getOperands().size() == 2;
		String operation = binaryOp.getOperations().get(0);
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		switch (operation) {
			case "+":
			case "-":
			case "*":
			case "/":
			case "<":
			case "<=":
			case ">":
			case ">=":
			case "==":
				return String.format("(%s %s %s)", evaluate(left), operation, evaluate(right));
			case "=":
				return String.format("(%s == %s)", evaluate(left), evaluate(right));
			default:
				return null;
		}
	}

	void execute(Statement stmt);

	default void execute(StmtConsume consume) {
		emitter().emit("channel_consume(self->%s_channel, sizeof(%s));", consume.getPort().getName(), type(types().portType(consume.getPort())));
	}

	default void execute(StmtWrite write) {
		if (write.getRepeatExpression() == null) {
			String portName = write.getPort().getName();
			String portType = type(types().portType(write.getPort()));
			String tmp = variables().generateTemp();
			emitter().emit("%s;", declaration(types().portType(write.getPort()), tmp));
			for (Expression expr : write.getValues()) {
				emitter().emit("%s = %s;", tmp, evaluate(expr));
				emitter().emit("channel_write(self->%s_channels, self->%1$s_count, (char*) &%s, sizeof(%s));", portName, tmp, portType);
			}
		} else {
			throw new Error("not implemented");
		}
	}

	default void execute(StmtAssignment assign) {
		Type type = types().lvalueType(assign.getLValue());
		String lvalue = lvalue(assign.getLValue());
		assign(type, lvalue, assign.getExpression());
	}

	default void execute(StmtBlock block) {
		for (VarDecl decl : block.getVarDecls()) {
			Type t = types().declaredType(decl);
			String d = declaration(t, decl.getName());
			emitter().emit("%s;", d);
			if (decl.getValue() != null) {
				assign(t, decl.getName(), decl.getValue());
			}
		}
		block.getStatements().forEach(this::execute);
	}

	String lvalue(LValue lvalue);

	default String lvalue(LValueVariable var) {
		return variables().name(var.getVariable());
	}
}

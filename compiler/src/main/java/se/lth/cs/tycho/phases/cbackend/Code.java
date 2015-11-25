package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.IntType;
import se.lth.cs.tycho.types.ListType;
import se.lth.cs.tycho.types.Type;
import se.lth.cs.tycho.types.UnitType;

import java.util.OptionalInt;

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
	default void assign(ListType type, String lvalue, Expression expr) {
		assignList(type, lvalue, expr);
	}

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

	void assignList(ListType type, String lvalue, Expression expr);
	default void assignList(ListType type, String lvalue, ExprInput input) {
		assert input.hasRepeat(); // only repeat assignments to lists are supported
		assert input.getPatternLength() == 1; // only with one variable
		assert input.getOffset() == 0; // and that variable is therefore the first
		Type portType = new ListType(types().portType(input.getPort()), OptionalInt.of(input.getRepeat()));
		emitter().emit("channel_peek(self->%s_channel, 0, sizeof(%s), (char*) &%s);", input.getPort().getName(), type(portType), lvalue);
	}
	default void assignList(ListType type, String lvalue, ExprVariable var) {
		assert type.getSize().isPresent();
		String tmp = variables().generateTemp();
		String rvalue = evaluate(var);
		emitter().emit("for (size_t %s=0; %1$s < %d; %1$s++) {", tmp, type.getSize().getAsInt());
		emitter().increaseIndentation();
		emitter().emit("%s[%s] = %s[%2$s];", lvalue, tmp, rvalue);
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	String declaration(Type type, String name);

	default String declaration(IntType type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(UnitType type, String name) { return "char " + name; }

	default String declaration(ListType type, String name) {
		if (type.getSize().isPresent()) {
			return String.format("%s %s[%d]", type(type.getElementType()), name, type.getSize().getAsInt());
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

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

	default String type(ListType type) {
		if (type.getSize().isPresent()) {
			return String.format("%s[%s]", type(type.getElementType()), type.getSize().getAsInt());
		} else {
			throw new RuntimeException("Not implemented");
		}
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

	default String evaluate(ExprIndexer indexer) {
		return String.format("%s[%s]", evaluate(indexer.getStructure()), evaluate(indexer.getIndex()));
	}

	void execute(Statement stmt);

	default void execute(StmtConsume consume) {
		emitter().emit("channel_consume(self->%s_channel, sizeof(%s)*%d);", consume.getPort().getName(), type(types().portType(consume.getPort())), consume.getNumberOfTokens());
	}

	default void execute(StmtWrite write) {
		String portName = write.getPort().getName();
		if (write.getRepeatExpression() == null) {
			String portType = type(types().portType(write.getPort()));
			String tmp = variables().generateTemp();
			emitter().emit("%s;", declaration(types().portType(write.getPort()), tmp));
			for (Expression expr : write.getValues()) {
				emitter().emit("%s = %s;", tmp, evaluate(expr));
				emitter().emit("channel_write(self->%s_channels, self->%1$s_count, (char*) &%s, sizeof(%s));", portName, tmp, portType);
			}
		} else if (write.getValues().size() == 1) {
			String portType = type(types().portTypeRepeated(write.getPort(), write.getRepeatExpression()));
			String value = evaluate(write.getValues().get(0));
			emitter().emit("channel_write(self->%s_channels, self->%1$s_count, (char*) &%s, sizeof(%s));", portName, value, portType);
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

	default String lvalue(LValueIndexer indexer) {
		return String.format("%s[%s]", lvalue(indexer.getStructure()), evaluate(indexer.getIndex()));
	}
}

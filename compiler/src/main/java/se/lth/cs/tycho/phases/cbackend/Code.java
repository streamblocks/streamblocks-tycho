package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.BoolType;
import se.lth.cs.tycho.types.IntType;
import se.lth.cs.tycho.types.ListType;
import se.lth.cs.tycho.types.QueueType;
import se.lth.cs.tycho.types.Type;
import se.lth.cs.tycho.types.UnitType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

	default Names names() { return backend().names(); }

	void assign(Type type, String lvalue, Expression expr);

	default void assign(IntType type, String lvalue, Expression expr) {
		assignScalar(type, lvalue, expr);
	}
	default void assign(BoolType type, String lvalue, Expression expr) {
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
		emitter().emit("channel_peek(self->%s_channel, %d, sizeof(%s), &%s);", input.getPort().getName(), input.getOffset(), type(portType), tmp);
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
	default void assignList(ListType type, String lvalue, ExprList list) {
		if (type.getSize().isPresent() && list.getGenerators().isEmpty()) {
			int i = 0;
			for (Expression element : list.getElements()) {
				assign(type.getElementType(), String.format("%s[%d]", lvalue, i), element);
				i = i + 1;
			}
		} else {
			emitter().emit("// dynamic list assignment is not implemented");
		}
	}

	String declaration(Type type, String name);

	default String declaration(IntType type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(UnitType type, String name) { return "char " + name; }

	default String declaration(ListType type, String name) {
		if (type.getSize().isPresent()) {
			return declaration(type.getElementType(), String.format("%s[%d]", name, type.getSize().getAsInt()));
		} else {
			return String.format("%s %s[] /* TODO IMPLEMENT */ ", type(type.getElementType()), name);
		}
	}

	default String declaration(QueueType type, String name) {
		return String.format("/* delcaration of internal queue */");
	}

	default String declaration(BoolType type, String name) { return "_Bool " + name; }

	String type(Type type);

	default String type(IntType type) {
		if (type.getSize().isPresent()) {
			int originalSize = type.getSize().getAsInt();
			int targetSize = 8;
			while (originalSize > targetSize) {
				targetSize = targetSize * 2;
			}
			return String.format(type.isSigned() ? "int%d_t" : "uint%d_t", targetSize);
		} else {
			return type.isSigned() ? "int64_t" : "uint64_t";
		}
	}

	default String type(UnitType type) {
		return "char";
	}

	default String type(ListType type) {
		if (type.getSize().isPresent()) {
			return String.format("%s[%s]", type(type.getElementType()), type.getSize().getAsInt());
		} else {
			return "void*";
		}
	}

	default String type(BoolType type) { return "_Bool"; }

	String evaluate(Expression expr);

	default String evaluate(ExprVariable variable) {
		return variables().name(variable.getVariable());
	}

	default String evaluate(ExprLiteral literal) {
		switch (literal.getKind()) {
			case Integer:
				return literal.getText();
			case True:
				return "true";
			case False:
				return "false";
			default:
				throw new UnsupportedOperationException(literal.getText());
		}
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
			case "!=":
			case "<<":
			case ">>":
			case "&":
			case "|":
			case "^":
				return String.format("(%s %s %s)", evaluate(left), operation, evaluate(right));
			case "=":
				return String.format("(%s == %s)", evaluate(left), evaluate(right));
			case "mod":
				return String.format("(%s %% %s)", evaluate(left), evaluate(right));
			case "and":
			case "&&":
				return String.format("(%s && /* TODO: IMPLEMET CORRECTLY */ %s)", evaluate(left), evaluate(right));
			case "||":
			case "or":
				return String.format("(%s || /* TODO: IMPLEMET CORRECTLY */ %s)", evaluate(left), evaluate(right));
			default:
				throw new UnsupportedOperationException(operation);
		}
	}

	default String evaluate(ExprUnaryOp unaryOp) {
		switch (unaryOp.getOperation()) {
			case "-":
			case "~":
				return String.format("%s(%s)", unaryOp.getOperation(), evaluate(unaryOp.getOperand()));
			case "not":
				return String.format("!%s", evaluate(unaryOp.getOperand()));
			default:
				throw new UnsupportedOperationException(unaryOp.getOperation());
		}
	}

	default String evaluate(ExprList list) {
		ListType t = (ListType) types().type(list);
		if (t.getSize().isPresent()) {
			if (list.getGenerators().isEmpty()) {
				String name = variables().generateTemp();
				String decl = declaration(t, name);
				String value = list.getElements().stream().sequential()
						.map(this::evaluate)
						.collect(Collectors.joining(", ", "{", "}"));
				emitter().emit("%s = %s;", decl, value);
				return name;
			} else {
				String name = variables().generateTemp();
				String decl = declaration(t, name);
				emitter().emit("%s;", decl);
				String index = variables().generateTemp();
				emitter().emit("size_t %s = 0;", index);
				for (GeneratorFilter gen : list.getGenerators()) {
					forEachGen(gen, () -> {
						for (Expression expr : list.getElements()) {
							emitter().emit("%s[%s++] = %s;", name, index, evaluate(expr));
						}
					});
				}
				return name;
			}
		} else {
			return "NULL /* TODO: implement dynamically sized lists */";
		}
	}

	default void forEachGen(GeneratorFilter gen, Runnable action) {
		if (gen.getFilters().isEmpty()) {
			forEach(gen.getCollectionExpr(), gen.getVariables(), action);
		} else {
			throw new UnsupportedOperationException("filters");
		}
	}

	void forEach(Expression collection, List<VarDecl> varDecls, Runnable action);
	default void forEach(ExprBinaryOp binOp, List<VarDecl> varDecls, Runnable action) {
		if (binOp.getOperations().equals(Collections.singletonList(".."))) {
			Type type = types().declaredType(varDecls.get(0));
			for (VarDecl d : varDecls) {
				emitter().emit("%s;", declaration(type, d.getName()));
			}
			String temp = variables().generateTemp();
			emitter().emit("%s = %s;", declaration(type, temp), evaluate(binOp.getOperands().get(0)));
			emitter().emit("while (%s <= %s) {", temp, evaluate(binOp.getOperands().get(1)));
			emitter().increaseIndentation();
			for (VarDecl d : varDecls) {
				emitter().emit("%s = %s++;", d.getName(), temp);
			}
			action.run();
			emitter().decreaseIndentation();
			emitter().emit("}");
		} else {
			throw new UnsupportedOperationException(binOp.getOperations().get(0));
		}
	}

	default String evaluate(ExprIndexer indexer) {
		return String.format("%s[%s]", evaluate(indexer.getStructure()), evaluate(indexer.getIndex()));
	}

	default String evaluate(ExprIf expr) {
		Type type = types().type(expr);
		String temp = variables().generateTemp();
		String decl = declaration(type, temp);
		emitter().emit("%s;", decl);
		emitter().emit("if (%s) {", evaluate(expr.getCondition()));
		emitter().increaseIndentation();
		assign(type, temp, expr.getThenExpr()); // this kind of assignment?
		emitter().decreaseIndentation();
		emitter().emit("} else {");
		emitter().increaseIndentation();
		assign(type, temp, expr.getThenExpr()); // this kind of assignment?
		emitter().decreaseIndentation();
		emitter().emit("}");
		return temp;
	}

	default String evaluate(ExprApplication apply) {
		String tmp = variables().generateTemp();
		String type = type(types().type(apply));
		emitter().emit("%s %s;", type, tmp);
		application(tmp, apply.getFunction(), apply.getArgs());
		return tmp;
	}

	default void application(String result, Expression func, List<Expression> args) {
		throw new UnsupportedOperationException();
	}
	default void application(String result, ExprVariable func, List<Expression> args) {
		VarDecl decl = names().declaration(func.getVariable());
		StringBuilder builder = new StringBuilder();
		builder.append(func.getVariable().getName())
				.append("(");
		IRNode parent = backend().tree().parent(decl);
		if (parent instanceof Scope) {
			builder.append("self, ");
		}
		for (Expression arg : args) {
			builder.append(evaluate(arg)).append(", ");
		}
		builder.append("&").append(result).append(");");
		emitter().emit(builder.toString());
	}

	default String evaluate(ExprLet let) {
		for (VarDecl decl : let.getVarDecls()) {
			emitter().emit("%s = %s;", declaration(types().declaredType(decl), decl.getName()), evaluate(decl.getValue()));
		}
		return evaluate(let.getBody());
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
				emitter().emit("channel_write(self->%s_channels, self->%1$s_count, &%s, sizeof(%s));", portName, tmp, portType);
			}
		} else if (write.getValues().size() == 1) {
			String portType = type(types().portTypeRepeated(write.getPort(), write.getRepeatExpression()));
			String value = evaluate(write.getValues().get(0));
			emitter().emit("channel_write(self->%s_channels, self->%1$s_count, &%s, sizeof(%s));", portName, value, portType);
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

	default void execute(StmtIf stmt) {
		emitter().emit("if (%s) {", evaluate(stmt.getCondition()));
		emitter().increaseIndentation();
		execute(stmt.getThenBranch());
		emitter().decreaseIndentation();
		if (stmt.getElseBranch() != null) {
			emitter().emit("} else {");
			emitter().increaseIndentation();
			execute(stmt.getElseBranch());
			emitter().decreaseIndentation();
		}
		emitter().emit("}");
	}

	default void execute(StmtForeach foreach) {
		forEachGenList(foreach.getGenerators(), 0, () -> execute(foreach.getBody()));
	}

	default void forEachGenList(List<GeneratorFilter> generators, int index, Runnable action) {
		if (index >= generators.size()) {
			action.run();
		} else {
			forEachGen(generators.get(index), () -> forEachGenList(generators, index+1, action));
		}
	}

	default void execute(StmtCall call) {
		call(call.getProcedure(), call.getArgs());
	}
	default void call(Expression proc, List<Expression> args) {
		throw new UnsupportedOperationException();
	}
	default void call(ExprVariable proc, List<Expression> args) {
		VarDecl decl = names().declaration(proc.getVariable());
		StringBuilder builder = new StringBuilder();
		builder.append(proc.getVariable().getName())
				.append("(");
		IRNode parent = backend().tree().parent(decl);
		if (parent instanceof Scope) {
			builder.append("self");
			if (!args.isEmpty()) {
				builder.append(", ");
			}
		}
		boolean first = true;
		for (Expression arg : args) {
			if (first) {
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(evaluate(arg));
		}
		builder.append(");");
		emitter().emit(builder.toString());
	}

	default void execute(StmtWhile stmt) {
		emitter().emit("while (true) {");
		emitter().increaseIndentation();
		emitter().emit("if (%s) break;", evaluate(stmt.getCondition()));
		execute(stmt.getBody());
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	String lvalue(LValue lvalue);

	default String lvalue(LValueVariable var) {
		return variables().name(var.getVariable());
	}

	default String lvalue(LValueIndexer indexer) {
		return String.format("%s[%s]", lvalue(indexer.getStructure()), evaluate(indexer.getIndex()));
	}
}

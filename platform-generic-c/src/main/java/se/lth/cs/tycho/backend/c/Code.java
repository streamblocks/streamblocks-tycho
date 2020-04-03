package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.GeneratorVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueDeref;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.type.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Module
public interface Code {
	@Binding(BindingKind.INJECTED)
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

	default MemoryStack memoryStack() { return backend().memoryStack(); }

	default String outputPortTypeSize(Port port) {
		Connection.End source = new Connection.End(Optional.of(backend().instance().get().getInstanceName()), port.getName());
		return backend().channels().sourceEndTypeSize(source);
	}

	default String inputPortTypeSize(Port port) {
		return backend().channels().targetEndTypeSize(new Connection.End(Optional.of(backend().instance().get().getInstanceName()), port.getName()));
	}

	default void copy(Type lvalueType, String lvalue, Type rvalueType, String rvalue) {
		emitter().emit("%s = %s;", lvalue, rvalue);
	}

	default void copy(ListType lvalueType, String lvalue, ListType rvalueType, String rvalue) {
		if (lvalueType.equals(rvalueType) && !isAlgebraicTypeList(lvalueType)) {
			emitter().emit("%s = %s;", lvalue, rvalue);
		} else {
			String index = variables().generateTemp();
			emitter().emit("for (size_t %1$s = 0; %1$s < %2$s; %1$s++) {", index, lvalueType.getSize().getAsInt());
			emitter().increaseIndentation();
			copy(lvalueType.getElementType(), String.format("%s.data[%s]", lvalue, index), rvalueType.getElementType(), String.format("%s.data[%s]", rvalue, index));
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
	}

	default void copy(AlgebraicType lvalueType, String lvalue, AlgebraicType rvalueType, String rvalue) {
		emitter().emit("copy_%s(&(%s), %s);", backend().algebraicTypes().type(lvalueType), lvalue, rvalue);
	}

	default boolean isAlgebraicTypeList(Type type) {
		if (!(type instanceof ListType)) {
			return false;
		}
		ListType listType = (ListType) type;
		if (listType.getElementType() instanceof AlgebraicType) {
			return true;
		} else {
			return isAlgebraicTypeList(listType.getElementType());
		}
	}

	default String compare(Type lvalueType, String lvalue, Type rvalueType, String rvalue) {
		return String.format("(%s == %s)", lvalue, rvalue);
	}

	default String compare(ListType lvalueType, String lvalue, ListType rvalueType, String rvalue) {
		String tmp = variables().generateTemp();
		String index = variables().generateTemp();
		emitter().emit("%s = true;", declaration(BoolType.INSTANCE, tmp));
		emitter().emit("for (size_t %1$s = 0; (%1$s < %2$s) && %3$s; %1$s++) {", index, lvalueType.getSize().getAsInt(), tmp);
		emitter().increaseIndentation();
		emitter().emit("%s &= %s;", tmp, compare(lvalueType.getElementType(), String.format("%s.data[%s]", lvalue, index), rvalueType.getElementType(), String.format("%s.data[%s]", rvalue, index)));
		emitter().decreaseIndentation();
		emitter().emit("}");
		return tmp;
	}

	default String compare(AlgebraicType lvalueType, String lvalue, AlgebraicType rvalueType, String rvalue) {
		String tmp = variables().generateTemp();
		emitter().emit("%s = compare_%s_t(%s, %s);", declaration(BoolType.INSTANCE, tmp), lvalueType.getName(), lvalue, rvalue);
		return tmp;
	}

	default String declaration(Type type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(UnitType type, String name) { return "char " + name; }

	default String declaration(RefType type, String name) {
		return declaration(type.getType(), String.format("(*%s)", name));
	}

	default String declaration(LambdaType type, String name) {
		String t = backend().callables().mangle(type).encode();
		return t + " " + name;
	}

	default String declaration(ProcType type, String name) {
		String t = backend().callables().mangle(type).encode();
		return t + " " + name;
	}

	default String declaration(BoolType type, String name) { return "_Bool " + name; }

	default String declaration(StringType type, String name) { return "char *" + name; }

	default String declaration(AlgebraicType type, String name) {
		return type(type) + " *" + name;
	}

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
			return type.isSigned() ? "int32_t" : "uint32_t";
		}
	}

	default String type(RealType type) {
		switch (type.getSize()) {
			case 32: return "float";
			case 64: return "double";
			default: throw new UnsupportedOperationException("Unknown real type.");
		}
	}

	default String type(UnitType type) {
		return "void";
	}

	default String type(ListType type) {
		return backend().callables().mangle(type).encode();
	}

	default String type(StringType type) {
		return "char*";
	}

	default String type(BoolType type) { return "_Bool"; }

	default String type(RefType type) { return type(type.getType()) + "*"; }

	default String type(AlgebraicType type) {
		return backend().algebraicTypes().type(type);
	}

	String evaluate(Expression expr);

	default String evaluate(ExprVariable variable) {
		return variables().name(variable.getVariable());
	}

	default String evaluate(ExprRef ref) {
		return "(&"+variables().name(ref.getVariable())+")";
	}

	default String evaluate(ExprDeref deref) {
		return "(*"+evaluate(deref.getReference())+")";
	}

	default String evaluate(ExprGlobalVariable variable) {
		return variables().globalName(variable);
	}

	default String evaluate(ExprLiteral literal) {
		switch (literal.getKind()) {
			case Integer:
				return literal.getText();
			case True:
				return "true";
			case False:
				return "false";
			case Real:
				return literal.getText();
			case String:
				return literal.getText();
			default:
				throw new UnsupportedOperationException(literal.getText());
		}
	}

	default String evaluate(ExprInput input) {
		String tmp = variables().generateTemp();
		Type type = types().type(input);
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		if (type instanceof AlgebraicType) {
			memoryStack().trackPointer(tmp, type);
		}
		if (input.hasRepeat()) {
		    if (input.getOffset() == 0) {
				emitter().emit("channel_peek_%s(self->%s_channel, 0, %d, %s.data);", inputPortTypeSize(input.getPort()), input.getPort().getName(), input.getRepeat(), tmp);
			} else {
		    	throw new RuntimeException("not implemented");
			}
		} else {
			if (input.getOffset() == 0) {
				emitter().emit("%s = channel_peek_first_%s(self->%s_channel);", tmp, inputPortTypeSize(input.getPort()), input.getPort().getName());
			} else {
				emitter().emit("channel_peek_%s(self->%s_channel, %d, 1, &%s);", inputPortTypeSize(input.getPort()), input.getPort().getName(), input.getOffset(), tmp);
			}
		}
		return tmp;
	}


	default String evaluate(ExprBinaryOp binaryOp) {
		assert binaryOp.getOperations().size() == 1 && binaryOp.getOperands().size() == 2;
		String operation = binaryOp.getOperations().get(0);
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		switch (operation) {
			case "==":
			case "=":
				return compare(types().type(left), evaluate(left), types().type(right), evaluate(right));
			case "!=":
				return "!" + compare(types().type(left), evaluate(left), types().type(right), evaluate(right));
			case "+":
			case "-":
			case "*":
			case "/":
			case "<":
			case "<=":
			case ">":
			case ">=":
			case "<<":
			case ">>":
			case "&":
			case "|":
			case "^":
				return String.format("(%s %s %s)", evaluate(left), operation, evaluate(right));
			case "mod":
				return String.format("(%s %% %s)", evaluate(left), evaluate(right));
			case "and":
			case "&&":
				String andResult = variables().generateTemp();
				emitter().emit("_Bool %s;", andResult);
				emitter().emit("if (%s) {", evaluate(left));
				emitter().increaseIndentation();
				memoryStack().enterScope();
				emitter().emit("%s = %s;", andResult, evaluate(right));
				memoryStack().exitScope();
				emitter().decreaseIndentation();
				emitter().emit("} else {");
				emitter().increaseIndentation();
				memoryStack().enterScope();
				emitter().emit("%s = false;", andResult);
				memoryStack().exitScope();
				emitter().decreaseIndentation();
				emitter().emit("}");
				return andResult;
			case "||":
			case "or":
				String orResult = variables().generateTemp();
				emitter().emit("_Bool %s;", orResult);
				emitter().emit("if (%s) {", evaluate(left));
				emitter().increaseIndentation();
				emitter().emit("%s = true;", orResult);
				emitter().decreaseIndentation();
				emitter().emit("} else {");
				emitter().increaseIndentation();
				memoryStack().enterScope();
				emitter().emit("%s = %s;", orResult, evaluate(right));
				memoryStack().exitScope();
				emitter().decreaseIndentation();
				emitter().emit("}");
				return orResult;
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

	default String evaluate(ExprComprehension comprehension) {
		return evaluateComprehension(comprehension, types().type(comprehension));
	}

	String evaluateComprehension(ExprComprehension comprehension, Type t);

	default String evaluateComprehension(ExprComprehension comprehension, ListType t) {
		String name = variables().generateTemp();
		String decl = declaration(t, name);
		emitter().emit("%s;", decl);
		String index = variables().generateTemp();
		emitter().emit("size_t %s = 0;", index);
		evaluateListComprehension(comprehension, name, index);
		return name;
	}

	void evaluateListComprehension(Expression comprehension, String result, String index);
	default void evaluateListComprehension(ExprComprehension comprehension, String result, String index) {
		if (!comprehension.getFilters().isEmpty()) {
			throw new UnsupportedOperationException("Filters in comprehensions not supported.");
		}
		withGenerator(comprehension.getGenerator().getCollection(), comprehension.getGenerator().getVarDecls(), () -> {
			evaluateListComprehension(comprehension.getCollection(), result, index);
		});
	}

	default void evaluateListComprehension(ExprList list, String result, String index) {
		list.getElements().forEach(element ->
				emitter().emit("%s.data[%s++] = %s;", result, index, evaluate(element))
		);
	}

	void withGenerator(Expression collection, ImmutableList<GeneratorVarDecl> varDecls, Runnable body);

	default void withGenerator(ExprBinaryOp binOp, ImmutableList<GeneratorVarDecl> varDecls, Runnable action) {
		if (binOp.getOperations().equals(Collections.singletonList(".."))) {
			String from = evaluate(binOp.getOperands().get(0));
			String to = evaluate(binOp.getOperands().get(1));
			for (VarDecl d : varDecls) {
				Type type = types().declaredType(d);
				String name = variables().declarationName(d);
				emitter().emit("%s = %s;", declaration(type, name), from);
				emitter().emit("while (%s <= %s) {", name, to);
				emitter().increaseIndentation();
				memoryStack().enterScope();
			}
			action.run();
			List<VarDecl> reversed = new ArrayList<>(varDecls);
			Collections.reverse(reversed);
			for (VarDecl d : reversed) {
				emitter().emit("%s++;", variables().declarationName(d));
				memoryStack().exitScope();
				emitter().decreaseIndentation();
				emitter().emit("}");
			}
		} else {
			throw new UnsupportedOperationException(binOp.getOperations().get(0));
		}
	}


	default String evaluate(ExprList list) {
		ListType t = (ListType) types().type(list);
		if (t.getSize().isPresent()) {
			String name = variables().generateTemp();
			Type elementType = t.getElementType();
			if (elementType instanceof AlgebraicType) {
				memoryStack().trackPointer(name, t);
			}
			String decl = declaration(t, name);
			String value = list.getElements().stream().sequential()
					.map(element -> {
						if (elementType instanceof AlgebraicType) {
							String tmp = variables().generateTemp();
							emitter().emit("%s = %s;", declaration(elementType, tmp), backend().defaultValues().defaultValue(elementType));
							copy(elementType, tmp, elementType , evaluate(element));
							return tmp;
						}
						return evaluate(element);
					})
					.collect(Collectors.joining(", ", "{ .data = {", "}}"));
			emitter().emit("%s = %s;", decl, value);
			return name;
		} else {
			return "NULL /* TODO: implement dynamically sized lists */";
		}
	}

	void forEach(Expression collection, List<GeneratorVarDecl> varDecls, Runnable action);

	default void forEach(ExprBinaryOp binOp, List<GeneratorVarDecl> varDecls, Runnable action) {
		emitter().emit("{");
		emitter().increaseIndentation();
		memoryStack().enterScope();
		if (binOp.getOperations().equals(Collections.singletonList(".."))) {
			Type type = types().declaredType(varDecls.get(0));
			for (VarDecl d : varDecls) {
				emitter().emit("%s;", declaration(type, variables().declarationName(d)));
			}
			String temp = variables().generateTemp();
			emitter().emit("%s = %s;", declaration(type, temp), evaluate(binOp.getOperands().get(0)));
			emitter().emit("while (%s <= %s) {", temp, evaluate(binOp.getOperands().get(1)));
			emitter().increaseIndentation();
			memoryStack().enterScope();
			for (VarDecl d : varDecls) {
				emitter().emit("%s = %s++;", variables().declarationName(d), temp);
			}
			action.run();
			memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		} else {
			throw new UnsupportedOperationException(binOp.getOperations().get(0));
		}
		memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default String evaluate(ExprIndexer indexer) {
		return String.format("%s.data[%s]", evaluate(indexer.getStructure()), evaluate(indexer.getIndex()));
	}

	default String evaluate(ExprIf expr) {
		Type type = types().type(expr);
		String temp = variables().generateTemp();
		if (type instanceof AlgebraicType) {
			memoryStack().trackPointer(temp, type);
		}
		String decl = declaration(type, temp);
		emitter().emit("%s = %s;", decl, backend().defaultValues().defaultValue(type));
		emitter().emit("if (%s) {", evaluate(expr.getCondition()));
		emitter().increaseIndentation();
		memoryStack().enterScope();
		Type thenType = types().type(expr.getThenExpr());
		String thenValue = evaluate(expr.getThenExpr());
		copy(type, temp, thenType, thenValue);
		memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("} else {");
		emitter().increaseIndentation();
		memoryStack().enterScope();
		Type elseType = types().type(expr.getElseExpr());
		String elseValue = evaluate(expr.getElseExpr());
		copy(type, temp, elseType, elseValue);
		memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
		return temp;
	}

	default String evaluate(ExprApplication apply) {
		Optional<String> directlyCallable = backend().callables().directlyCallableName(apply.getFunction());
		String fn;
		List<String> parameters = new ArrayList<>();
		if (directlyCallable.isPresent()) {
			fn = directlyCallable.get();
			parameters.add("NULL");
		} else {
			String name = evaluate(apply.getFunction());
			fn = name + ".f";
			parameters.add(name+".env");
		}
		for (Expression parameter : apply.getArgs()) {
			String param = evaluate(parameter);
			Type type = types().type(parameter);
			parameters.add(passByValue(param, type));
		}
		Type type = types().type(apply);
		String result = variables().generateTemp();
		String decl = declaration(type, result);
		if ((type instanceof AlgebraicType) || (isAlgebraicTypeList(type))) {
			memoryStack().trackPointer(result, type);
		}
		emitter().emit("%s = %s(%s);", decl, fn, String.join(", ", parameters));
		return result;
	}

	default String evaluate(ExprLambda lambda) {
		backend().emitter().emit("// begin evaluate(ExprLambda)");
		String functionName = backend().callables().functionName(lambda);
		String env = backend().callables().environmentName(lambda);
		for (VarDecl var : backend().callables().closure(lambda)) {
			emitter().emit("%s.%s = %s;", env, variables().declarationName(var), variables().reference(var));
		}

		Type type = backend().types().type(lambda);
		String typeName = backend().callables().mangle(type).encode();
		String funPtr = backend().variables().generateTemp();
		backend().emitter().emit("%s %s = { &%s, &%s };", typeName, funPtr, functionName, env);

		backend().emitter().emit("// end evaluate(ExprLambda)");
		return funPtr;
	}

	default String evaluate(ExprProc proc) {
		backend().emitter().emit("// begin evaluate(ExprProc)");
		String functionName = backend().callables().functionName(proc);
		String env = backend().callables().environmentName(proc);
		for (VarDecl var : backend().callables().closure(proc)) {
			emitter().emit("%s.%s = %s;", env, variables().declarationName(var), variables().reference(var));
		}

		Type type = backend().types().type(proc);
		String typeName = backend().callables().mangle(type).encode();
		String funPtr = backend().variables().generateTemp();
		backend().emitter().emit("%s %s = { &%s, &%s };", typeName, funPtr, functionName, env);

		backend().emitter().emit("// end evaluate(ExprProc)");
		return funPtr;
	}

	default String evaluate(ExprLet let) {
		let.forEachChild(backend().callables()::declareEnvironmentForCallablesInScope);
		for (VarDecl decl : let.getVarDecls()) {
			Type type = types().declaredType(decl);
			String name = variables().declarationName(decl);
			emitter().emit("%s = %s;", declaration(type, name), backend().defaultValues().defaultValue(type));
			copy(type, name, types().type(decl.getValue()), evaluate(decl.getValue()));
		}
		return evaluate(let.getBody());
	}

	default String evaluate(ExprTypeConstruction construction) {
		String fn = backend().algebraicTypes().constructor(construction.getConstructor());
		List<String> parameters = new ArrayList<>();
		for (Expression parameter : construction.getArgs()) {
			parameters.add(evaluate(parameter));
		}
		String result = variables().generateTemp();
		String decl = declaration(types().type(construction), result);
		emitter().emit("%s = %s(%s);", decl, fn, String.join(", ", parameters));
		memoryStack().trackPointer(result, types().type(construction));
		return result;
	}

	default String evaluate(ExprTypeAssertion assertion) {
		Type type = types().type(assertion.getType());
		String result = variables().generateTemp();
		String decl = declaration(type, result);
		emitter().emit("%s = (%s)(%s);", decl, type(type) + (type instanceof AlgebraicType ? "*" : ""), evaluate(assertion.getExpression()));
		return result;
	}

	default String evaluate(ExprField field) {
		return String.format("%s->%s", evaluate(field.getStructure()), field.getField().getName());
	}

	default String evaluate(ExprCase caseExpr) {
		return backend().patternMatching().evaluate(caseExpr);
	}

	void execute(Statement stmt);

	default void execute(StmtConsume consume) {
		emitter().emit("channel_consume_%s(self->%s_channel, %d);", inputPortTypeSize(consume.getPort()), consume.getPort().getName(), consume.getNumberOfTokens());
	}

	default void execute(StmtWrite write) {
		String portName = write.getPort().getName();
		if (write.getRepeatExpression() == null) {
			String portType = type(types().portType(write.getPort()));
			String tmp = variables().generateTemp();
			emitter().emit("%s;", declaration(types().portType(write.getPort()), tmp));
			for (Expression expr : write.getValues()) {
				emitter().emit("%s = %s;", tmp, evaluate(expr));
				emitter().emit("channel_write_one_%s(self->%s_channels, %s);", outputPortTypeSize(write.getPort()), portName, tmp);
			}
		} else if (write.getValues().size() == 1) {
			String portType = type(types().portType(write.getPort()));
			String value = evaluate(write.getValues().get(0));
			String repeat = evaluate(write.getRepeatExpression());
			String temp = variables().generateTemp();
			emitter().emit("for (size_t %1$s = 0; %1$s < %2$s; %1$s++) {", temp, repeat);
			emitter().increaseIndentation();
			emitter().emit("channel_write_one_%1$s(self->%2$s_channels, %3$s.data[%4$s]);", outputPortTypeSize(write.getPort()), portName, value, temp);
			emitter().decreaseIndentation();
			emitter().emit("}");
		} else {
			throw new Error("not implemented");
		}
	}

	default void execute(StmtAssignment assign) {
		memoryStack().enterScope();
		Type type = types().lvalueType(assign.getLValue());
		String lvalue = lvalue(assign.getLValue());
		copy(type, lvalue, types().type(assign.getExpression()), evaluate(assign.getExpression()));
		memoryStack().exitScope();
	}

	default void execute(StmtBlock block) {
		emitter().emit("{");
		emitter().increaseIndentation();
		memoryStack().enterScope();
		backend().callables().declareEnvironmentForCallablesInScope(block);
		for (VarDecl decl : block.getVarDecls()) {
			Type t = types().declaredType(decl);
			String declarationName = variables().declarationName(decl);
			String d = declaration(t, declarationName);
			if (t instanceof AlgebraicType) {
				memoryStack().trackPointer(declarationName, t);
			}
			emitter().emit("%s = %s;", d, backend().defaultValues().defaultValue(t));
			if (decl.getValue() != null) {
				copy(t, declarationName, types().type(decl.getValue()), evaluate(decl.getValue()));
			}
		}
		block.getStatements().forEach(this::execute);
		memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void execute(StmtIf stmt) {
		memoryStack().enterScope();
		emitter().emit("if (%s) {", evaluate(stmt.getCondition()));
		emitter().increaseIndentation();
		memoryStack().enterScope();
		stmt.getThenBranch().forEach(this::execute);
		memoryStack().exitScope();
		emitter().decreaseIndentation();
		if (stmt.getElseBranch() != null) {
			emitter().emit("} else {");
			emitter().increaseIndentation();
			memoryStack().enterScope();
			stmt.getElseBranch().forEach(this::execute);
			memoryStack().exitScope();
			emitter().decreaseIndentation();
		}
		emitter().emit("}");
		memoryStack().exitScope();
	}

	default void execute(StmtForeach foreach) {
		forEach(foreach.getGenerator().getCollection(), foreach.getGenerator().getVarDecls(), () -> {
			for (Expression filter : foreach.getFilters()) {
				emitter().emit("if (%s) {", evaluate(filter));
				emitter().increaseIndentation();
				memoryStack().enterScope();
			}
			foreach.getBody().forEach(this::execute);
			for (Expression filter : foreach.getFilters()) {
				memoryStack().exitScope();
				emitter().decreaseIndentation();
				emitter().emit("}");
			}
		});
	}

	default void execute(StmtCall call) {
		memoryStack().enterScope();
		Optional<String> directlyCallable = backend().callables().directlyCallableName(call.getProcedure());
		String proc;
		List<String> parameters = new ArrayList<>();
		if (directlyCallable.isPresent()) {
			proc = directlyCallable.get();
			parameters.add("NULL");
		} else {
			String name = evaluate(call.getProcedure());
			proc = name + ".f";
			parameters.add(name + ".env");
		}
		for (Expression parameter : call.getArgs()) {
			String param = evaluate(parameter);
			Type type = types().type(parameter);
			parameters.add(passByValue(param, type));
		}
		emitter().emit("%s(%s);", proc, String.join(", ", parameters));
		memoryStack().exitScope();
	}

	default void execute(StmtWhile stmt) {
		emitter().emit("while (true) {");
		emitter().increaseIndentation();
		memoryStack().enterScope();
		emitter().emit("if (!%s) break;", evaluate(stmt.getCondition()));
		stmt.getBody().forEach(this::execute);
		memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void execute(StmtCase caseStmt) {
		backend().patternMatching().execute(caseStmt);
	}

	String lvalue(LValue lvalue);

	default String lvalue(LValueVariable var) {
		return variables().name(var.getVariable());
	}

	default String lvalue(LValueDeref deref) {
		return "(*"+lvalue(deref.getVariable())+")";
	}

	default String lvalue(LValueIndexer indexer) {
		return String.format("%s.data[%s]", lvalue(indexer.getStructure()), evaluate(indexer.getIndex()));
	}

	default String lvalue(LValueField field) {
		return String.format("%s->%s", lvalue(field.getStructure()), field.getField().getName());
	}

	default String passByValue(String param, Type type) {
		return param;
	}

	default String passByValue(String param, AlgebraicType type) {
		String tmp = variables().generateTemp();
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		copy(type, tmp, type, param);
		memoryStack().trackPointer(tmp, type);
		return tmp;
	}

	default String passByValue(String param, ListType type) {
		if (!isAlgebraicTypeList(type)) {
			return param;
		}
		String tmp = variables().generateTemp();
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		copy(type, tmp, type, param);
		memoryStack().trackPointer(tmp, type);
		return tmp;
	}

	default String returnValue(String result, Type type) {
		return result;
	}

	default String returnValue(String result, AlgebraicType type) {
		String tmp = variables().generateTemp();
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		copy(type, tmp, type, result);
		return tmp;
	}

	default String returnValue(String result, ListType type) {
		if (!isAlgebraicTypeList(type)) {
			return result;
		}
		String tmp = variables().generateTemp();
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		copy(type, tmp, type, result);
		return tmp;
	}
}

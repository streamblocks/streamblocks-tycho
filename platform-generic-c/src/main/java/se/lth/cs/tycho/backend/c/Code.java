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
import se.lth.cs.tycho.ir.stmt.lvalue.LValueNth;
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

	default Trackable trackable() { return backend().trackable(); }

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
		emitter().emit("copy_%s(&(%s), %s);", backend().algebraic().utils().name(lvalueType), lvalue, rvalue);
	}

	default void copy(AliasType lvalueType, String lvalue, AliasType rvalueType, String rvalue) {
		copy(lvalueType.getType(), lvalue, rvalueType.getType(), rvalue);
	}

	default void copy(TupleType lvalueType, String lvalue, TupleType rvalueType, String rvalue) {
		copy(backend().tuples().convert().apply(lvalueType), lvalue, backend().tuples().convert().apply(rvalueType), rvalue);
	}

	default boolean isAlgebraicTypeList(Type type) {
		if (!(type instanceof ListType)) {
			return false;
		}
		ListType listType = (ListType) type;
		if (listType.getElementType() instanceof AlgebraicType || backend().alias().isAlgebraicType(listType.getElementType())) {
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
		emitter().emit("%s = compare_%s(%s, %s);", declaration(BoolType.INSTANCE, tmp), backend().algebraic().utils().name(lvalueType), lvalue, rvalue);
		return tmp;
	}

	default String compare(AliasType lvalueType, String lvalue, AliasType rvalueType, String rvalue) {
		return compare(lvalueType.getType(), lvalue, rvalueType.getType(), rvalue);
	}

	default String compare(TupleType lvalueType, String lvalue, TupleType rvalueType, String rvalue) {
		return compare(backend().tuples().convert().apply(lvalueType), lvalue, backend().tuples().convert().apply(rvalueType), rvalue);
	}

	default String declaration(Type type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(UnitType type, String name) { return "char " + name; }

	default String declaration(RefType type, String name) {
		return declaration(type.getType(), String.format("(*%s)", name));
	}

	default String declaration(LambdaType type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(ProcType type, String name) {
		return type(type) + " " + name;
	}

	default String declaration(BoolType type, String name) { return "_Bool " + name; }

	default String declaration(StringType type, String name) { return "char *" + name; }

	default String declaration(AlgebraicType type, String name) {
		return type(type) + " *" + name;
	}

	default String declaration(AliasType type, String name) {
		return type(type) + (backend().alias().isAlgebraicType(type) ? " *" : " ") + name;
	}

	default String declaration(TupleType type, String name) {
		return declaration(backend().tuples().convert().apply(type), name);
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

	default String type(CharType type) { return "char"; }

	default String type(RefType type) { return type(type.getType()) + "*"; }

	default String type(AlgebraicType type) {
		return backend().algebraic().utils().name(type);
	}

	default String type(AliasType type) {
		return type.getName();
	}

	default String type(TupleType type) {
		return type(backend().tuples().convert().apply(type));
	}

	default String type(LambdaType type) {
		return backend().callables().mangle(type).encode();
	}

	default String type(ProcType type) {
		return backend().callables().mangle(type).encode();
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
			case Char:
				return literal.getText();
			default:
				throw new UnsupportedOperationException(literal.getText());
		}
	}

	default String evaluate(ExprInput input) {
		String tmp = variables().generateTemp();
		Type type = types().type(input);
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		trackable().track(tmp, type);
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
		Type lhs = types().type(binaryOp.getOperands().get(0));
		Type rhs = types().type(binaryOp.getOperands().get(1));
		String operation = binaryOp.getOperations().get(0);
		switch (operation) {
			case "+":
				return evaluateBinaryAdd(lhs, rhs, binaryOp);
			case "-":
				return evaluateBinarySub(lhs, rhs, binaryOp);
			case "*":
				return evaluateBinaryTimes(lhs, rhs, binaryOp);
			case "/":
				return evaluateBinaryDiv(lhs, rhs, binaryOp);
			case "div":
				return evaluateBinaryIntDiv(lhs, rhs, binaryOp);
			case "%":
			case "mod":
				return evaluateBinaryMod(lhs, rhs, binaryOp);
			case "^":
				return evaluateBinaryExp(lhs, rhs, binaryOp);
			case "&":
				return evaluateBinaryBitAnd(lhs, rhs, binaryOp);
			case "<<":
				return evaluateBinaryShiftL(lhs, rhs, binaryOp);
			case ">>":
				return evaluateBinaryShiftR(lhs, rhs, binaryOp);
			case "&&":
			case "and":
				return evaluateBinaryAnd(lhs, rhs, binaryOp);
			case "|":
				return evaluateBinaryBitOr(lhs, rhs, binaryOp);
			case "||":
			case "or":
				return evaluateBinaryOr(lhs, rhs, binaryOp);
			case "=":
			case "==":
				return evaluateBinaryEq(lhs, rhs, binaryOp);
			case "!=":
				return evaluateBinaryNeq(lhs, rhs, binaryOp);
			case "<":
				return evaluateBinaryLtn(lhs, rhs, binaryOp);
			case "<=":
				return evaluateBinaryLeq(lhs, rhs, binaryOp);
			case ">":
				return evaluateBinaryGtn(lhs, rhs, binaryOp);
			case ">=":
				return evaluateBinaryGeq(lhs, rhs, binaryOp);
			case "in":
				return evaluateBinaryIn(lhs, rhs, binaryOp);
			default:
				throw new UnsupportedOperationException(operation);
		}
	}
	
	default String evaluateBinaryAdd(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryAdd(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s + %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinarySub(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinarySub(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s - %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryTimes(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryTimes(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s * %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryDiv(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryDiv(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s / %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryIntDiv(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryIntDiv(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s / %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryMod(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryMod(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s % %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryExp(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryExp(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s << %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryMod(RealType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("pow(%s, %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryBitAnd(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryBitAnd(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s & %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryShiftL(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryShiftL(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s << %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryShiftR(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryShiftR(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s >> %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryAnd(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryAnd(BoolType lhs, BoolType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		String andResult = variables().generateTemp();
		emitter().emit("_Bool %s;", andResult);
		emitter().emit("if (%s) {", evaluate(left));
		emitter().increaseIndentation();
		trackable().enter();
		emitter().emit("%s = %s;", andResult, evaluate(right));
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("} else {");
		emitter().increaseIndentation();
		trackable().enter();
		emitter().emit("%s = false;", andResult);
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("}");
		return andResult;
	}
	
	default String evaluateBinaryBitOr(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryBitOr(IntType lhs, IntType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s | %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryOr(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryOr(BoolType lhs, BoolType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		String orResult = variables().generateTemp();
		emitter().emit("_Bool %s;", orResult);
		emitter().emit("if (%s) {", evaluate(left));
		emitter().increaseIndentation();
		emitter().emit("%s = true;", orResult);
		emitter().decreaseIndentation();
		emitter().emit("} else {");
		emitter().increaseIndentation();
		trackable().enter();
		emitter().emit("%s = %s;", orResult, evaluate(right));
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("}");
		return orResult;
	}

	default String evaluateBinaryEq(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return compare(types().type(left), evaluate(left), types().type(right), evaluate(right));
	}
	
	default String evaluateBinaryNeq(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return "!" + compare(types().type(left), evaluate(left), types().type(right), evaluate(right));
	}

	default String evaluateBinaryLtn(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryLtn(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s < %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryLeq(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryLeq(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s <= %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryGtn(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryGtn(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s > %s)", evaluate(left), evaluate(right));
	}
	
	default String evaluateBinaryGeq(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluateBinaryGeq(NumberType lhs, NumberType rhs, ExprBinaryOp binaryOp) {
		Expression left = binaryOp.getOperands().get(0);
		Expression right = binaryOp.getOperands().get(1);
		return String.format("(%s >= %s)", evaluate(left), evaluate(right));
	}

	default String evaluateBinaryIn(Type lhs, Type rhs, ExprBinaryOp binaryOp) {
		throw new UnsupportedOperationException(binaryOp.getOperations().get(0));
	}

	default String evaluate(ExprUnaryOp unaryOp) {
		switch (unaryOp.getOperation()) {
			case "-":
				return evaluateUnaryMinus(types().type(unaryOp.getOperand()), unaryOp);
			case "~":
				return evaluateUnaryInvert(types().type(unaryOp.getOperand()), unaryOp);
			case "!":
			case "not":
				return evaluateUnaryNot(types().type(unaryOp.getOperand()), unaryOp);
			case "dom":
				return evaluateUnaryDom(types().type(unaryOp.getOperand()), unaryOp);
			case "rng":
				return evaluateUnaryRng(types().type(unaryOp.getOperand()), unaryOp);
			case "#":
				return evaluateUnarySize(types().type(unaryOp.getOperand()), unaryOp);
			default:
				throw new UnsupportedOperationException(unaryOp.getOperation());
		}
	}

	default String evaluateUnaryMinus(Type type, ExprUnaryOp expr) {
		throw new UnsupportedOperationException(expr.getOperation());
	}

	default String evaluateUnaryMinus(NumberType type, ExprUnaryOp expr) {
		return String.format("-(%s)",evaluate(expr.getOperand()));
	}

	default String evaluateUnaryInvert(Type type, ExprUnaryOp expr) {
		throw new UnsupportedOperationException(expr.getOperation());
	}

	default String evaluateUnaryInvert(IntType type, ExprUnaryOp expr) {
		return String.format("~(%s)",evaluate(expr.getOperand()));
	}

	default String evaluateUnaryNot(Type type, ExprUnaryOp expr) {
		throw new UnsupportedOperationException(expr.getOperation());
	}

	default String evaluateUnaryNot(BoolType type, ExprUnaryOp expr) {
		return String.format("!(%s)",evaluate(expr.getOperand()));
	}

	default String evaluateUnaryDom(Type type, ExprUnaryOp expr) {
		throw new UnsupportedOperationException(expr.getOperation());
	}

	default String evaluateUnaryRng(Type type, ExprUnaryOp expr) {
		throw new UnsupportedOperationException(expr.getOperation());
	}

	default String evaluateUnarySize(Type type, ExprUnaryOp expr) {
		throw new UnsupportedOperationException(expr.getOperation());
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
				trackable().enter();
			}
			action.run();
			List<VarDecl> reversed = new ArrayList<>(varDecls);
			Collections.reverse(reversed);
			for (VarDecl d : reversed) {
				emitter().emit("%s++;", variables().declarationName(d));
				trackable().exit();
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
			trackable().track(name, t);
			String decl = declaration(t, name);
			String value = list.getElements().stream().sequential()
					.map(element -> {
						if (elementType instanceof AlgebraicType || backend().alias().isAlgebraicType(elementType)) {
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
		trackable().enter();
		if (binOp.getOperations().equals(Collections.singletonList(".."))) {
			Type type = types().declaredType(varDecls.get(0));
			for (VarDecl d : varDecls) {
				emitter().emit("%s;", declaration(type, variables().declarationName(d)));
			}
			String temp = variables().generateTemp();
			emitter().emit("%s = %s;", declaration(type, temp), evaluate(binOp.getOperands().get(0)));
			emitter().emit("while (%s <= %s) {", temp, evaluate(binOp.getOperands().get(1)));
			emitter().increaseIndentation();
			trackable().enter();
			for (VarDecl d : varDecls) {
				emitter().emit("%s = %s++;", variables().declarationName(d), temp);
			}
			action.run();
			trackable().exit();
			emitter().decreaseIndentation();
			emitter().emit("}");
		} else {
			throw new UnsupportedOperationException(binOp.getOperations().get(0));
		}
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default String evaluate(ExprIndexer indexer) {
		return String.format("%s.data[%s]", evaluate(indexer.getStructure()), evaluate(indexer.getIndex()));
	}

	default String evaluate(ExprTuple tuple) {
		String fn = backend().tuples().utils().constructor((TupleType) types().type(tuple));
		List<String> parameters = new ArrayList<>();
		for (Expression parameter : tuple.getElements()) {
			parameters.add(evaluate(parameter));
		}
		String result = variables().generateTemp();
		String decl = declaration(types().type(tuple), result);
		trackable().track(result, types().type(tuple));
		emitter().emit("%s = %s(%s);", decl, fn, String.join(", ", parameters));
		return result;
	}

	default String evaluate(ExprNth nth) {
		return String.format("%s->%s", evaluate(nth.getStructure()), "_" + nth.getNth().getNumber());
	}

	default String evaluate(ExprIf expr) {
		Type type = types().type(expr);
		String temp = variables().generateTemp();
		trackable().track(temp, type);
		String decl = declaration(type, temp);
		emitter().emit("%s = %s;", decl, backend().defaultValues().defaultValue(type));
		emitter().emit("if (%s) {", evaluate(expr.getCondition()));
		emitter().increaseIndentation();
		trackable().enter();
		Type thenType = types().type(expr.getThenExpr());
		String thenValue = evaluate(expr.getThenExpr());
		copy(type, temp, thenType, thenValue);
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("} else {");
		emitter().increaseIndentation();
		trackable().enter();
		Type elseType = types().type(expr.getElseExpr());
		String elseValue = evaluate(expr.getElseExpr());
		copy(type, temp, elseType, elseValue);
		trackable().exit();
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
		trackable().track(result, type);
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
		String fn = backend().algebraic().utils().constructor(construction.getConstructor());
		List<String> parameters = new ArrayList<>();
		for (Expression parameter : construction.getArgs()) {
			parameters.add(evaluate(parameter));
		}
		String result = variables().generateTemp();
		String decl = declaration(types().type(construction), result);
		emitter().emit("%s = %s(%s);", decl, fn, String.join(", ", parameters));
		trackable().track(result, types().type(construction));
		return result;
	}

	default String evaluate(ExprTypeAssertion assertion) {
		Type type = types().type(assertion.getType());
		String result = variables().generateTemp();
		String decl = declaration(type, result);
		emitter().emit("%s = (%s)(%s);", decl, type(type) + (type instanceof AlgebraicType || backend().alias().isAlgebraicType(type) ? "*" : ""), evaluate(assertion.getExpression()));
		return result;
	}

	default String evaluate(ExprField field) {
		return String.format("%s->%s", evaluate(field.getStructure()), field.getField().getName());
	}

	default String evaluate(ExprCase caseExpr) {
		return backend().patmat().evaluate(caseExpr);
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
		trackable().enter();
		Type type = types().type(assign.getLValue());
		String lvalue = lvalue(assign.getLValue());
		copy(type, lvalue, types().type(assign.getExpression()), evaluate(assign.getExpression()));
		trackable().exit();
	}

	default void execute(StmtBlock block) {
		emitter().emit("{");
		emitter().increaseIndentation();
		trackable().enter();
		backend().callables().declareEnvironmentForCallablesInScope(block);
		for (VarDecl decl : block.getVarDecls()) {
			Type t = types().declaredType(decl);
			String declarationName = variables().declarationName(decl);
			String d = declaration(t, declarationName);
			trackable().track(declarationName, t);
			emitter().emit("%s = %s;", d, backend().defaultValues().defaultValue(t));
			if (decl.getValue() != null) {
				copy(t, declarationName, types().type(decl.getValue()), evaluate(decl.getValue()));
			}
		}
		block.getStatements().forEach(this::execute);
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void execute(StmtIf stmt) {
		trackable().enter();
		emitter().emit("if (%s) {", evaluate(stmt.getCondition()));
		emitter().increaseIndentation();
		trackable().enter();
		stmt.getThenBranch().forEach(this::execute);
		trackable().exit();
		emitter().decreaseIndentation();
		if (stmt.getElseBranch() != null) {
			emitter().emit("} else {");
			emitter().increaseIndentation();
			trackable().enter();
			stmt.getElseBranch().forEach(this::execute);
			trackable().exit();
			emitter().decreaseIndentation();
		}
		emitter().emit("}");
		trackable().exit();
	}

	default void execute(StmtForeach foreach) {
		forEach(foreach.getGenerator().getCollection(), foreach.getGenerator().getVarDecls(), () -> {
			for (Expression filter : foreach.getFilters()) {
				emitter().emit("if (%s) {", evaluate(filter));
				emitter().increaseIndentation();
				trackable().enter();
			}
			foreach.getBody().forEach(this::execute);
			for (Expression filter : foreach.getFilters()) {
				trackable().exit();
				emitter().decreaseIndentation();
				emitter().emit("}");
			}
		});
	}

	default void execute(StmtCall call) {
		trackable().enter();
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
		trackable().exit();
	}

	default void execute(StmtWhile stmt) {
		emitter().emit("while (true) {");
		emitter().increaseIndentation();
		trackable().enter();
		emitter().emit("if (!%s) break;", evaluate(stmt.getCondition()));
		stmt.getBody().forEach(this::execute);
		trackable().exit();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void execute(StmtCase caseStmt) {
		backend().patmat().execute(caseStmt);
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

	default String lvalue(LValueNth nth) {
		return String.format("%s->%s", lvalue(nth.getStructure()), "_" + nth.getNth().getNumber());
	}

	default String passByValue(String param, Type type) {
		return param;
	}

	default String passByValue(String param, AlgebraicType type) {
		String tmp = variables().generateTemp();
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		copy(type, tmp, type, param);
		trackable().track(tmp, type);
		return tmp;
	}

	default String passByValue(String param, ListType type) {
		if (!isAlgebraicTypeList(type)) {
			return param;
		}
		String tmp = variables().generateTemp();
		emitter().emit("%s = %s;", declaration(type, tmp), backend().defaultValues().defaultValue(type));
		copy(type, tmp, type, param);
		trackable().track(tmp, type);
		return tmp;
	}

	default String passByValue(String param, TupleType type) {
		return passByValue(param, backend().tuples().convert().apply(type));
	}

	default String passByValue(String param, AliasType type) {
		return passByValue(param, type.getConcreteType());
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

	default String returnValue(String result, TupleType type) {
		return returnValue(result, backend().tuples().convert().apply(type));
	}

	default String returnValue(String result, AliasType type) {
		return returnValue(result, type.getConcreteType());
	}
}

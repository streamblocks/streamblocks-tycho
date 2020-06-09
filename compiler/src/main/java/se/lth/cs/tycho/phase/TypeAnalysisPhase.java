package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprNth;
import se.lth.cs.tycho.ir.expr.ExprTypeAssertion;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.type.*;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TypeAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		task.getSourceUnits().forEach(sourceUnit -> {
			Class<? extends TypeChecker> module;
			switch (sourceUnit.getLanguage()) {
				case CAL: module = CalTypeChecker.class; break;
				case ORCC: module = OrccTypeChecker.class; break;
				default: module = CalTypeChecker.class; break;
			}
			TypeChecker checker = MultiJ.from(module)
					.bind("types").to(task.getModule(Types.key))
					.bind("reporter").to(context.getReporter())
					.bind("tree").to(task.getModule(TreeShadow.key))
					.bind("typeScopes").to(task.getModule(TypeScopes.key))
					.instance();
			checker.accept(sourceUnit);
		});
		return task;
	}

	@Module
	interface CalTypeChecker extends TypeChecker {

		default boolean isConvertible(Type to, Type from) {
			return isAssignable(to, from);
		}

		default boolean isConvertible(IntType to, IntType from) {
			return true;
		}

		default boolean isConvertible(RealType to, RealType from) {
			return true;
		}

		default boolean isConvertible(AliasType to, AliasType from) {
			return isConvertible(to.getType(), from.getType());
		}

		default boolean isConvertible(AliasType to, Type from) {
			return isConvertible(to.getType(), from);
		}

		default boolean isConvertible(Type to, AliasType from) {
			return isConvertible(to, from.getType());
		}

		default boolean isAssertable(Type to, Type from) {
			return to.equals(from);
		}
		default boolean isAssertable(NumberType to, NumberType from) {
			return true;
		}
		default boolean isAssertable(IntType to, RealType from) {
			return true;
		}
		default boolean isAssertable(RealType to, IntType from) {
			return true;
		}
		default boolean isAssertable(AliasType to, AliasType from) {
			return isAssertable(to.getType(), from.getType());
		}
		default boolean isAssertable(AliasType to, Type from) {
			return isAssertable(to.getType(), from);
		}
		default boolean isAssertable(Type to, AliasType from) {
			return isAssertable(to, from.getType());
		}

		default boolean isAssignable(Type to, Type from) {
			return false;
		}
		default boolean isAssignable(ErrorType to, Type from) {
			return false;
		}
		default boolean isAssignable(BottomType to, Type from) {
			return false;
		}
		default boolean isAssignable(Type to, ErrorType from) {
			return true;
		}
		default boolean isAssignable(Type to, BottomType from) {
			return true;
		}
		default boolean isAssignable(BottomType to, BottomType from) { return true; }
		default boolean isAssignable(ErrorType to, ErrorType from) { return true; }
		default boolean isAssignable(TopType to, Type from) {
			return true;
		}
		default boolean isAssignable(TopType to, ErrorType from) {
			return true;
		}
		default boolean isAssignable(TopType to, BottomType from) {
			return true;
		}
		default boolean isAssignable(IntType to, IntType from) {
			if (!to.isSigned() && from.isSigned()) {
				return false;
			}
			if (!to.getSize().isPresent()) {
				return true;
			}
			if (from.getSize().isPresent()) {
				if (to.isSigned() == from.isSigned()) {
					return to.getSize().getAsInt() >= from.getSize().getAsInt();
				} else {
					return to.getSize().getAsInt() > from.getSize().getAsInt();
				}
			} else {
				if (to.getSize().isPresent()) {
					return true;
				} else {
					return false;
				}
			}
		}
		default boolean isAssignable(RealType to, RealType from) {
			return to.getSize() >= from.getSize();
		}
		default boolean isAssignable(RealType to, IntType from) {
			return true;
		}
		default boolean isAssignable(BoolType to, BoolType from) {
			return true;
		}
		default boolean isAssignable(CharType to, CharType from) {
			return true;
		}
		default boolean isAssignable(UnitType to, UnitType from) {
			return true;
		}
		default boolean isAssignable(ListType to, ListType from) {
			if (!to.getSize().isPresent() || to.getSize().equals(from.getSize())) {
				return isAssignable(to.getElementType(), from.getElementType());
			} else {
				return false;
			}
		}
		default boolean isAssignable(ListType to, RangeType from) {
			return isAssignable(to, new ListType(from.getType(), from.getLength()));
		}

		default boolean isAssignable(RangeType to, RangeType from) {
			if (to.getLength().equals(from.getLength())) {
				return true;
			}
			if (!to.getLength().isPresent()) {
				return true;
			}
			return false;
		}

		default boolean isAssignable(CallableType to, CallableType from) {
			if (to.getParameterTypes().size() != from.getParameterTypes().size()) {
				return false;
			}
			if (!isAssignable(to.getReturnType(), from.getReturnType())) {
				return false;
			}
			Iterator<Type> toParIter = to.getParameterTypes().iterator();
			Iterator<Type> fromParIter = from.getParameterTypes().iterator();
			while (toParIter.hasNext() && fromParIter.hasNext()) {
				Type toPar = toParIter.next();
				Type fromPar = fromParIter.next();
				if (!isAssignable(fromPar, toPar)) {
					return false;
				}
			}
			return !toParIter.hasNext() && !fromParIter.hasNext();
		}

		default boolean isAssignable(AlgebraicType to, AlgebraicType from) {
			return to.equals(from);
		}

		default boolean isAssignable(AliasType to, AliasType from) {
			return isAssignable(to.getType(), from.getType());
		}

		default boolean isAssignable(AliasType to, Type from) {
			return isAssignable(to.getType(), from);
		}

		default boolean isAssignable(Type to, AliasType from) {
			return isAssignable(to, from.getType());
		}

		default boolean isAssignable(TupleType to, TupleType from) {
			return to.getTypes().size() == from.getTypes().size()
					&& IntStream
						.range(0, to.getTypes().size())
						.allMatch(i -> isAssignable(to.getTypes().get(i), from.getTypes().get(i)));
		}
	}

	@Module
	interface OrccTypeChecker extends TypeChecker {

		default boolean isConvertible(Type to, Type from) {
			return isAssignable(to, from);
		}
		default boolean isConvertible(IntType to, IntType from) {
			return true;
		}

		default boolean isConvertible(RealType to, RealType from) {
			return true;
		}

		@Override
		default boolean isAssertable(Type to, Type from) {
			return false;
		}

		default boolean isAssignable(Type to, Type from) {
			return to.equals(from);
		}
		default boolean isAssignable(StringType to, IntType from) {
			return true;
		}
		default boolean isAssignable(ErrorType to, Type from) {
			return false;
		}
		default boolean isAssignable(BottomType to, Type from) {
			return false;
		}
		default boolean isAssignable(Type to, ErrorType from) {
			return true;
		}
		default boolean isAssignable(Type to, BottomType from) {
			return true;
		}
		default boolean isAssignable(TopType to, Type from) {
			return true;
		}
		default boolean isAssignable(TopType to, ErrorType from) {
			return true;
		}
		default boolean isAssignable(TopType to, BottomType from) {
			return true;
		}
		default boolean isAssignable(IntType to, IntType from) {
			return true;
		}
		default boolean isAssignable(RealType to, RealType from) {
			return true;
		}
		default boolean isAssignable(BoolType to, BoolType from) {
			return true;
		}
		default boolean isAssignable(CharType to, CharType from) {
			return true;
		}
		default boolean isAssignable(UnitType to, UnitType from) {
			return true;
		}
		default boolean isAssignable(ListType to, ListType from) {
			return isAssignable(to.getElementType(), from.getElementType());
		}
		default boolean isAssignable(CallableType to, CallableType from) {
			if (to.getParameterTypes().size() != from.getParameterTypes().size()) {
				return false;
			}
			if (!isAssignable(to.getReturnType(), from.getReturnType())) {
				return false;
			}
			Iterator<Type> toParIter = to.getParameterTypes().iterator();
			Iterator<Type> fromParIter = from.getParameterTypes().iterator();
			while (toParIter.hasNext() && fromParIter.hasNext()) {
				Type toPar = toParIter.next();
				Type fromPar = fromParIter.next();
				if (!isAssignable(fromPar, toPar)) {
					return false;
				}
			}
			return !toParIter.hasNext() && !fromParIter.hasNext();
		}
	}

	interface TypeChecker {

		@Binding(BindingKind.INJECTED)
		Types types();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		boolean isAssignable(Type to, Type from);

		boolean isAssertable(Type to, Type from);

		boolean isConvertible(Type to, Type from);

		default void check(Type expected, Type actual, IRNode node) {
			if (!isAssignable(expected, actual)) {
				if (isConvertible(expected, actual)) {
					reporter().report(new Diagnostic(Diagnostic.Kind.WARNING, "Unsafe conversion from " + actual + " to " + expected + ".", sourceUnit(node), node));
				} else {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Incompatible types; expected " + expected + " but was " + actual + ".", sourceUnit(node), node));
				}
			}
		}

		default void accept(IRNode node) {
			typecheck(node);
			node.forEachChild(this::accept);
		}

		default void typecheck(IRNode node) {

		}

		default void typecheck(ExprApplication expr) {
			Type type = types().type(expr.getFunction());
			if (!(type instanceof LambdaType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a function.", sourceUnit(expr.getFunction()), expr.getFunction()));
			} else {
				CallableType callableType = (LambdaType) type;
				typecheckArgs(expr, callableType, expr.getArgs());
			}
		}

		default void typecheck(ExprBinaryOp expr) {
			String op = expr.getOperations().get(0);
			Type lhs = types().type(expr.getOperands().get(0));
			Type rhs = types().type(expr.getOperands().get(1));
			if (!(isBinaryOpSupported(op, lhs, rhs))) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, op + " is not supported with " + lhs + " and " + rhs + ".", sourceUnit(expr), expr));
			}
		}

		default void typecheck(ExprCase expr) {
			Type expected = types().type(expr.getScrutinee());
			expr.getAlternatives().forEach(alternative -> {
				check(expected, types().type(alternative.getPattern()), alternative.getPattern());
			});
			expr.getAlternatives().forEach(alternative -> {
				alternative.getGuards().forEach(guard -> check(BoolType.INSTANCE, types().type(guard), guard));
			});
		}

		default void typecheck(ExprComprehension expr) {
			Type type = types().type(expr.getCollection());
			if (!(type instanceof CollectionType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a collection.", sourceUnit(expr.getCollection()), expr.getCollection()));
			}
			expr.getFilters().forEach(filter -> check(BoolType.INSTANCE, types().type(filter), filter));
		}

		default void typecheck(ExprField expr) {
			Type type =  types().type(expr.getStructure());
			if (!(type instanceof CaseAnalysisPhase.Space.Product)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a product.", sourceUnit(expr.getStructure()), expr.getStructure()));
			}
		}

		default void typecheck(ExprIf expr) {
			check(BoolType.INSTANCE, types().type(expr.getCondition()), expr.getCondition());
		}

		default void typecheck(ExprIndexer expr) {
			Type type = types().type(expr.getStructure());
			if (type instanceof ListType) {
				check(new IntType(OptionalInt.empty(), false), types().type(expr.getIndex()), expr.getIndex());
			} else if (type instanceof MapType) {
				check(((MapType) type).getKeyType(), types().type(expr.getIndex()), expr.getIndex());
			} else {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a supported operation.", sourceUnit(expr), expr));
			}
		}

		default void typecheck(ExprNth expr) {
			Type type = types().type(expr.getStructure());
			if (!(type instanceof TupleType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a tuple.", sourceUnit(expr.getStructure()), expr.getStructure()));
			}
		}

		default void typecheck(ExprTypeAssertion expr) {
			Type expected = types().type(expr.getType());
			Type actual = types().type(expr.getExpression());
			if (!isAssertable(expected, actual)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot type assert " + actual + " to " + expected + ".", sourceUnit(expr), expr));
			}
		}

		default void typecheck(ExprTypeConstruction expr) {
			Type type = types().type(expr);
			if (!(type instanceof AlgebraicType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not an algebraic type.", sourceUnit(expr), expr));
			} else if (type instanceof ProductType) {
				ProductType productType = (ProductType) type;
				CallableType callableType = new CallableType(productType.getFields().stream().map(FieldType::getType).collect(Collectors.toList()), type) {};
				typecheckArgs(expr, callableType, expr.getArgs());
			} else if (type instanceof SumType) {
				SumType sumType = (SumType) type;
				SumType.VariantType variantType = sumType.getVariants().stream().filter(variant -> Objects.equals(variant.getName(), expr.getConstructor())).findAny().get();
				CallableType callableType = new CallableType(variantType.getFields().stream().map(FieldType::getType).collect(Collectors.toList()), type) {};
				typecheckArgs(expr, callableType, expr.getArgs());
			}
		}

		default void typecheck(ExprUnaryOp expr) {
			String op = expr.getOperation();
			Type type = types().type(expr.getOperand());
			if (!(isUnaryOpSupported(op, type))) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, op + " is not supported with " + type + ".", sourceUnit(expr), expr));
			}
		}

		default void typecheck(StmtAssignment stmt) {
			check(types().type(stmt.getLValue()), types().type(stmt.getExpression()), stmt.getExpression());
		}

		default void typecheck(StmtCall stmt) {
			Type type = types().type(stmt.getProcedure());
			if (!(type instanceof ProcType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a procedure.", sourceUnit(stmt), stmt.getProcedure()));
			} else {
				CallableType callableType = (ProcType) type;
				typecheckArgs(stmt, callableType, stmt.getArgs());
			}
		}

		default void typecheck(StmtCase stmt) {
			Type expected = types().type(stmt.getScrutinee());
			stmt.getAlternatives().forEach(alternative -> {
				check(expected, types().type(alternative.getPattern()), alternative.getPattern());
			});
			stmt.getAlternatives().forEach(alternative -> {
				alternative.getGuards().forEach(guard -> check(BoolType.INSTANCE, types().type(guard), guard));
			});
		}

		default void typecheck(StmtForeach stmt) {
			stmt.getFilters().forEach(filter -> check(BoolType.INSTANCE, types().type(filter), filter));
		}

		default void typecheck(StmtIf stmt) {
			check(BoolType.INSTANCE, types().type(stmt.getCondition()), stmt.getCondition());
		}

		default void typecheck(StmtRead stmt) {
			Type actual;
			if (stmt.getRepeatExpression() != null) {
				actual = types().portTypeRepeated(stmt.getPort(), stmt.getRepeatExpression());
			} else {
				actual = types().portType(stmt.getPort());
			}
			stmt.getLValues().forEach(lvalue -> check(types().type(lvalue), actual, lvalue));
		}

		default void typecheck(StmtWhile stmt) {
			check(BoolType.INSTANCE, types().type(stmt.getCondition()), stmt.getCondition());
		}

		default void typecheck(StmtWrite stmt) {
			Type expected;
			if (stmt.getRepeatExpression() != null) {
				expected = types().portTypeRepeated(stmt.getPort(), stmt.getRepeatExpression());
			} else {
				expected = types().portType(stmt.getPort());
			}
			stmt.getValues().forEach(value -> check(expected, types().type(value), value));
		}

		default void typecheck(VarDecl decl) {
			if (decl instanceof PatternVarDecl) {
				return;
			}
			if (decl.getValue() != null && decl.getType() != null) {
				check(types().declaredType(decl), types().type(decl.getValue()), decl);
			} else if (decl.getType() == null) {
				Type t = types().declaredType(decl);
				if (t == TopType.INSTANCE || t == BottomType.INSTANCE) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not infer a type for " + decl.getName() + ".", sourceUnit(decl), decl));
				}
			}
		}

		default void typecheck(OutputExpression output) {
			Type expected;
			if (output.getRepeatExpr() != null) {
				expected = types().portTypeRepeated(output.getPort(), output.getRepeatExpr());
			} else {
				expected = types().portType(output.getPort());
			}
			output.getExpressions().forEach(expr -> check(expected, types().type(expr), expr));
		}

		default void typecheck(PatternDeconstruction pattern) {
			typeScopes().construction(pattern).ifPresent(decl -> {
				GlobalTypeDecl type = (GlobalTypeDecl) decl;
				if (type instanceof ProductTypeDecl) {
					ProductTypeDecl product = (ProductTypeDecl) type;
					Iterator<Type> types = product.getFields().stream().map(field -> types().type(field.getType())).collect(Collectors.toList()).iterator();
					Iterator<Pattern> patterns = pattern.getPatterns().iterator();
					while (types.hasNext() && patterns.hasNext()) {
						Pattern p = patterns.next();
						Type patType = types().type(p);
						Type parType = types.next();
						check(parType, patType, p);
					}
					if (types.hasNext() || patterns.hasNext()) {
						final int expected = product.getFields().size();
						final int actual = pattern.getPatterns().size();
						reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Wrong number of arguments; expected " + expected + ", but was " + actual + ".", sourceUnit(pattern), pattern));
					}
				} else if (type instanceof SumTypeDecl) {
					SumTypeDecl sum = (SumTypeDecl) type;
					SumTypeDecl.VariantDecl variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), pattern.getDeconstructor())).findAny().get();
					Iterator<Type> types = variant.getFields().stream().map(field -> types().type(field.getType())).collect(Collectors.toList()).iterator();
					Iterator<Pattern> patterns = pattern.getPatterns().iterator();
					while (types.hasNext() && patterns.hasNext()) {
						Pattern p = patterns.next();
						Type patType = types().type(p);
						Type parType = types.next();
						check(parType, patType, p);
					}
					if (types.hasNext() || patterns.hasNext()) {
						final int expected = variant.getFields().size();
						final int actual = pattern.getPatterns().size();
						reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Wrong number of arguments; expected " + expected + ", but was " + actual + ".", sourceUnit(pattern), pattern));
					}
				}
			});
		}

		default void typecheck(Generator generator) {
			Type type = types().type(generator.getCollection());
			if (!(type instanceof CollectionType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a collection.", sourceUnit(generator.getCollection()), generator.getCollection()));
			}
		}

		default void typecheckArgs(IRNode node, CallableType callableType, List<Expression> args) {
			Iterator<Type> typeIter = callableType.getParameterTypes().iterator();
			Iterator<Expression> exprIter = args.iterator();
			while (typeIter.hasNext() && exprIter.hasNext()) {
				Type parType = typeIter.next();
				Expression expr = exprIter.next();
				Type argType = types().type(expr);
				check(parType, argType, expr);
			}
			if (typeIter.hasNext() || exprIter.hasNext()) {
				final int expected = callableType.getParameterTypes().size();
				final int actual = args.size();
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Wrong number of arguments; expected " + expected + ", but was " + actual + ".", sourceUnit(node), node));
			}
		}

		default boolean isUnaryOpSupported(String op, Type type) {
			switch (op) {
				case "-":
					return testMinusSupport(type);
				case "~":
					return testInvertSupport(type);
				case "!":
				case "not":
					return testNotSupport(type);
				case "dom":
					return testDomSupport(type);
				case "rng":
					return testRngSupport(type);
				case "#":
					return testSizeSupport(type);
				default:
					return false;
			}
		}

		default boolean isUnaryOpSupported(String op, AliasType type) {
			return isUnaryOpSupported(op, type.getConcreteType());
		}

		default boolean testMinusSupport(Type type) {
			return false;
		}

		default boolean testMinusSupport(NumberType type) {
			return true;
		}

		default boolean testInvertSupport(Type type) {
			return false;
		}

		default boolean testInvertSupport(IntType type) {
			return true;
		}

		default boolean testNotSupport(Type type) {
			return false;
		}

		default boolean testNotSupport(BoolType type) {
			return true;
		}

		default boolean testDomSupport(Type type) {
			return false;
		}

		default boolean testDomSupport(MapType type) {
			return true;
		}

		default boolean testRngSupport(Type type) {
			return false;
		}

		default boolean testRngSupport(MapType type) {
			return true;
		}

		default boolean testSizeSupport(Type type) {
			return false;
		}

		default boolean testSizeSupport(CollectionType type) {
			return false;
		}

		default boolean isBinaryOpSupported(String op, Type a, Type b) {
			switch (op) {
				case "+":
					return testAddSupport(a, b);
				case "-":
					return testSubSupport(a, b);
				case "*":
					return testTimesSupport(a, b);
				case "/":
					return testDivSupport(a, b);
				case "div":
					return testIntDivSupport(a, b);
				case "%":
				case "mod":
					return testModSupport(a, b);
				case "^":
					return testExpSupport(a, b);
				case "&":
					return testBitAndSupport(a, b);
				case "<<":
					return testShiftLSupport(a, b);
				case ">>":
					return testShiftRSupport(a, b);
				case "&&":
				case "and":
					return testAndSupport(a, b);
				case "|":
					return testBitOrSupport(a, b);
				case "||":
				case "or":
					return testOrSupport(a, b);
				case "=":
				case "==":
					return testEqSupport(a, b);
				case "!=":
					return testNeqSupport(a, b);
				case "<":
					return testLtnSupport(a, b);
				case "<=":
					return testLeqSupport(a, b);
				case ">":
					return testGtnSupport(a, b);
				case ">=":
					return testGeqSupport(a, b);
				case "in":
					return testInSupport(a, b);
				default:
					return false;
			}
		}

		default boolean isBinaryOpSupported(String op, AliasType a, Type b) {
			return isBinaryOpSupported(op, a.getConcreteType(), b);
		}

		default boolean isBinaryOpSupported(String op, Type a, AliasType b) {
			return isBinaryOpSupported(op, a, b.getConcreteType());
		}

		default boolean testAddSupport(Type a, Type b) {
			return false;
		}

		default boolean testAddSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testAddSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testAddSupport(ListType a, ListType b) {
			return isAssignable(a, b);
		}

		default boolean testAddSupport(MapType a, MapType b) {
			return isAssignable(a, b);
		}

		default boolean testSubSupport(Type a, Type b) {
			return false;
		}

		default boolean testSubSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testSubSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testTimesSupport(Type a, Type b) {
			return false;
		}

		default boolean testTimesSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testTimesSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testDivSupport(Type a, Type b) {
			return false;
		}

		default boolean testDivSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testDivSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testIntDivSupport(Type a, Type b) {
			return false;
		}

		default boolean testIntDivSupport(IntType a, IntType b) {
			return true;
		}

		default boolean testModSupport(Type a, Type b) {
			return false;
		}

		default boolean testModSupport(IntType a, IntType b) {
			return true;
		}

		default boolean testExpSupport(Type a, Type b) {
			return false;
		}

		default boolean testExpSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testBitAndSupport(Type a, Type b) {
			return false;
		}

		default boolean testBitAndSupport(IntType a, IntType b) {
			return true;
		}

		default boolean testShiftLSupport(Type a, Type b) {
			return false;
		}

		default boolean testShiftLSupport(IntType a, IntType b) {
			return true;
		}

		default boolean testShiftRSupport(Type a, Type b) {
			return false;
		}

		default boolean testShiftRSupport(IntType a, IntType b) {
			return true;
		}

		default boolean testAndSupport(Type a, Type b) {
			return false;
		}

		default boolean testBitOrSupport(Type a, Type b) {
			return false;
		}

		default boolean testBitOrSupport(IntType a, IntType b) {
			return true;
		}

		default boolean testAndSupport(BoolType a, BoolType b) {
			return true;
		}

		default boolean testOrSupport(Type a, Type b) {
			return false;
		}

		default boolean testOrSupport(BoolType a, BoolType b) {
			return true;
		}

		default boolean testEqSupport(Type a, Type b) {
			return isAssignable(a, b);
		}

		default boolean testNeqSupport(Type a, Type b) {
			return isAssignable(a, b);
		}

		default boolean testLtnSupport(Type a, Type b) {
			return false;
		}

		default boolean testLtnSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testLtnSupport(StringType a, StringType b) {
			return true;
		}

		default boolean testLtnSupport(CharType a, CharType b) {
			return true;
		}

		default boolean testLtnSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testLeqSupport(Type a, Type b) {
			return false;
		}

		default boolean testLeqSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testLeqSupport(StringType a, StringType b) {
			return true;
		}

		default boolean testLeqSupport(CharType a, CharType b) {
			return true;
		}

		default boolean testLeqSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testGtnSupport(Type a, Type b) {
			return false;
		}

		default boolean testGtnSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testGtnSupport(StringType a, StringType b) {
			return true;
		}

		default boolean testGtnSupport(CharType a, CharType b) {
			return true;
		}

		default boolean testGtnSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testGeqSupport(Type a, Type b) {
			return false;
		}

		default boolean testGeqSupport(NumberType a, NumberType b) {
			return true;
		}

		default boolean testGeqSupport(StringType a, StringType b) {
			return true;
		}

		default boolean testGeqSupport(CharType a, CharType b) {
			return true;
		}

		default boolean testGeqSupport(SetType a, SetType b) {
			return isAssignable(a, b);
		}

		default boolean testInSupport(Type a, Type b) {
			return false;
		}

		default boolean testInSupport(Type a, ListType b) {
			return isAssignable(a, b.getElementType());
		}

		default boolean testInSupport(Type a, SetType b) {
			return isAssignable(a, b.getElementType());
		}

		default boolean testInSupport(Type a, MapType b) {
			return isAssignable(a, b.getKeyType());
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}

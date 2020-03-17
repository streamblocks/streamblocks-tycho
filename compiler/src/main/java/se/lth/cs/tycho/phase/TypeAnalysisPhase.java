package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprTypeAssertion;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstructor;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.type.*;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
					.bind("sourceUnit").to(sourceUnit)
					.bind("typeScopes").to(task.getModule(TypeScopes.key))
					.instance();
			checker.check(sourceUnit);
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

		default boolean isAssertable(Type to, Type from) {
			return to.equals(from);
		}
		default boolean isAssertable(IntType to, IntType from) {
			return true;
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
			}else{
				if(to.getSize().isPresent()){
					return true;
				}else{
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
	}

	@Module
	interface OrccTypeChecker extends TypeChecker {

		default boolean isConvertible(Type to, Type from) {
			return isAssignable(to, from);
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
		SourceUnit sourceUnit();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		default void check(IRNode node) {
			checkTypes(node);
			node.forEachChild(this::check);
		}

		boolean isAssertable(Type a, Type b);

		boolean isAssignable(Type a, Type b);

		boolean isConvertible(Type a, Type b);

		default void checkAssignment(Type to, Type from, IRNode node) {
			if (!isAssignable(to, from)) {
				if (isConvertible(to, from)) {
					reporter().report(new Diagnostic(Diagnostic.Kind.WARNING, "Unsafe conversion from " + from + " to " + to + ".", sourceUnit(), node));
				} else {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Incompatible types; expected " + to + " but was " + from + ".", sourceUnit(), node));
				}
			}
		}

		default void checkTypes(IRNode node) {}

		default void checkTypes(StmtAssignment assignment) {
			checkAssignment(
					types().lvalueType(assignment.getLValue()),
					types().type(assignment.getExpression()),
					assignment);
		}

		default void checkTypes(VarDecl varDecl) {
			if (varDecl.getValue() != null && varDecl.getType() != null) {
				checkAssignment(
						types().declaredType(varDecl),
						types().type(varDecl.getValue()),
						varDecl);
			} else if (varDecl.getType() == null) {
				Type t = types().declaredType(varDecl);
				if (t == TopType.INSTANCE || t == BottomType.INSTANCE) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not infer a type for " + varDecl.getName() + ".", sourceUnit(), varDecl));
				}
			}
		}

		default void checkTypes(ExprApplication apply) {
			Type type = types().type(apply.getFunction());
			if (!(type instanceof LambdaType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a function.", sourceUnit(), apply.getFunction()));
			} else {
				CallableType callableType = (LambdaType) type;
				checkArguments(apply, callableType, apply.getArgs());
			}
		}

		default void checkTypes(ExprTypeConstruction construction) {
			Type type = types().type(construction);
			if (!(type instanceof AlgebraicType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a user type.", sourceUnit(), construction));
			} else if (type instanceof ProductType) {
				ProductType productType = (ProductType) type;
				CallableType callableType = new CallableType(productType.getFields().stream().map(FieldType::getType).collect(Collectors.toList()), type) {};
				checkArguments(construction, callableType, construction.getArgs());
			} else if (type instanceof SumType) {
				SumType sumType = (SumType) type;
				SumType.VariantType variantType = sumType.getVariants().stream().filter(variant -> Objects.equals(variant.getName(), construction.getConstructor())).findAny().get();
				CallableType callableType = new CallableType(variantType.getFields().stream().map(FieldType::getType).collect(Collectors.toList()), type) {};
				checkArguments(construction, callableType, construction.getArgs());
			}
		}

		default void checkTypes(StmtCall call) {
			Type type = types().type(call.getProcedure());
			if (!(type instanceof ProcType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Not a procedure.", sourceUnit(), call.getProcedure()));
			} else {
				CallableType callableType = (ProcType) type;
				checkArguments(call, callableType, call.getArgs());
			}
		}

		default void checkTypes(StmtRead read) {
			Type actual;
			if (read.getRepeatExpression() != null) {
				actual = types().portTypeRepeated(read.getPort(), read.getRepeatExpression());
			} else {
				actual = types().portType(read.getPort());
			}
			for (LValue lvalue : read.getLValues()) {
				Type expected = types().lvalueType(lvalue);
				checkAssignment(expected, actual, lvalue);
			}
		}

		default void checkTypes(OutputExpression output) {
			Type expected;
			if (output.getRepeatExpr() != null) {
				expected = types().portTypeRepeated(output.getPort(), output.getRepeatExpr());
			} else {
				expected = types().portType(output.getPort());
			}
			for (Expression value : output.getExpressions()) {
				Type actual = types().type(value);
				checkAssignment(expected, actual, value);
			}
		}

		default void checkTypes(StmtWrite write) {
			Type expected;
			if (write.getRepeatExpression() != null) {
				expected = types().portTypeRepeated(write.getPort(), write.getRepeatExpression());
			} else {
				expected = types().portType(write.getPort());
			}
			for (Expression value : write.getValues()) {
				Type actual = types().type(value);
				checkAssignment(expected, actual, value);
			}
		}

		default void checkTypes(ExprTypeAssertion assertion) {
			Type to = types().type(assertion.getType());
			Type from = types().type(assertion.getExpression());
			if (!isAssertable(to, from)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot type assert to " + to + ".", sourceUnit(), assertion));
			}
		}

		default void checkTypes(ExprCase caseExpr) {
			Type type = types().type(caseExpr.getExpression());

			if (!(type instanceof AlgebraicType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not pattern matched a non algebraic data type.", sourceUnit(), caseExpr.getExpression()));
			}

			caseExpr.getAlternatives().forEach(alternative -> {
				alternative.getGuards().forEach(guard -> {
					if (types().type(guard) instanceof AlgebraicType) {
						reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Guard cannot be an algebraic data type.", sourceUnit(), guard));
					}
				});
			});

			caseExpr.getAlternatives().forEach(alternative -> {
				if (!types().type(alternative.getPattern()).equals(type)) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Deconstructor " + ((PatternDeconstructor) alternative.getPattern()).getName() + " does not exist for type " + type, sourceUnit(), alternative.getPattern()));
				}
			});
		}

		default void checkTypes(StmtCase caseStmt) {
			Type type = types().type(caseStmt.getExpression());

			if (!(type instanceof AlgebraicType)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not pattern matched a non algebraic data type.", sourceUnit(), caseStmt.getExpression()));
			}

			caseStmt.getAlternatives().forEach(alternative -> {
				alternative.getGuards().forEach(guard -> {
					if (types().type(guard) instanceof AlgebraicType) {
						reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Guard cannot be an algebraic data type.", sourceUnit(), guard));
					}
				});
			});

			caseStmt.getAlternatives().forEach(alternative -> {
				if (!types().type(alternative.getPattern()).equals(type)) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Deconstructor " + ((PatternDeconstructor) alternative.getPattern()).getName() + " does not exist for type " + type, sourceUnit(), alternative.getPattern()));
				}
			});
		}

		default void checkTypes(PatternDeconstructor deconstructor) {
			typeScopes().construction(deconstructor).ifPresent(decl -> {
				GlobalTypeDecl type = (GlobalTypeDecl) decl;
				if (type.getDeclaration() instanceof ProductTypeDecl) {
					ProductTypeDecl product = (ProductTypeDecl) type.getDeclaration();
					Iterator<Type> types = product.getFields().stream().map(field -> types().type(field.getType())).collect(Collectors.toList()).iterator();
					Iterator<Pattern> patterns = deconstructor.getPatterns().iterator();
					while (types.hasNext() && patterns.hasNext()) {
						Pattern pattern = patterns.next();
						Type patType = types().type(pattern);
						Type parType = types.next();
						checkAssignment(parType, patType, pattern);
					}
					if (types.hasNext() || patterns.hasNext()) {
						final int expected = product.getFields().size();
						final int actual = deconstructor.getPatterns().size();
						reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Wrong number of arguments; expected " + expected + ", but was " + actual + ".", sourceUnit(), deconstructor));
					}
				} else if (type.getDeclaration() instanceof SumTypeDecl) {
					SumTypeDecl sum = (SumTypeDecl) type.getDeclaration();
					SumTypeDecl.VariantDecl variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), deconstructor.getName())).findAny().get();
					Iterator<Type> types = variant.getFields().stream().map(field -> types().type(field.getType())).collect(Collectors.toList()).iterator();
					Iterator<Pattern> patterns = deconstructor.getPatterns().iterator();
					while (types.hasNext() && patterns.hasNext()) {
						Pattern pattern = patterns.next();
						Type patType = types().type(pattern);
						Type parType = types.next();
						checkAssignment(parType, patType, pattern);
					}
					if (types.hasNext() || patterns.hasNext()) {
						final int expected = variant.getFields().size();
						final int actual = deconstructor.getPatterns().size();
						reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Wrong number of arguments; expected " + expected + ", but was " + actual + ".", sourceUnit(), deconstructor));
					}
				}
			});
		}

		default void checkArguments(IRNode node, CallableType callableType, List<Expression> args) {
			Iterator<Type> typeIter = callableType.getParameterTypes().iterator();
			Iterator<Expression> exprIter = args.iterator();
			while (typeIter.hasNext() && exprIter.hasNext()) {
				Type parType = typeIter.next();
				Expression expr = exprIter.next();
				Type argType = types().type(expr);
				checkAssignment(parType, argType, expr);
			}
			if (typeIter.hasNext() || exprIter.hasNext()) {
				final int expected = callableType.getParameterTypes().size();
				final int actual = args.size();
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Wrong number of arguments; expected " + expected + ", but was " + actual + ".", sourceUnit(), node));
			}
		}
	}
}

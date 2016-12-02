package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.types.*;

import java.util.Iterator;
import java.util.List;

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
					.bind("types").to(context.getAttributeManager().getAttributeModule(Types.key, task))
					.bind("reporter").to(context.getReporter())
					.bind("sourceUnit").to(sourceUnit)
					.instance();
			checker.check(sourceUnit);
		});
		return task;
	}

	@Module
	interface CalTypeChecker  extends TypeChecker {

		default boolean isConvertible(Type to, Type from) {
			return isAssignable(to, from);
		}

		default boolean isConvertible(IntType to, IntType from) {
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
			}
			return false;
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
	}

	@Module
	interface OrccTypeChecker  extends TypeChecker {

		default boolean isConvertible(Type to, Type from) {
			return isAssignable(to, from);
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

		@Binding
		Reporter reporter();

		@Binding
		SourceUnit sourceUnit();

		default void check(IRNode node) {
			checkTypes(node);
			node.forEachChild(this::check);
		}

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

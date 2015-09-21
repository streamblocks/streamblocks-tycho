package se.lth.cs.tycho.phases;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.BindingKind;
import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
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
			TypeChecker checker = MultiJ.from(OrccTypeChecker.class)
					.bind("types").to(context.getAttributeManager().getAttributeModule(Types.key, task))
					.bind("reporter").to(context.getReporter())
					.bind("sourceUnit").to(sourceUnit)
					.instance();
			checker.check(sourceUnit);
		});
		return null;
	}

	@Module
	interface OrccTypeChecker  extends TypeChecker {

		default boolean isAssignable(Type to, Type from) {
			return false;
		}
		default boolean isAssignable(BottomType to, Type from) {
			return false;
		}
		default boolean isAssignable(Type to, BottomType from) {
			return true;
		}
		default boolean isAssignable(TopType to, Type from) {
			return true;
		}
		default boolean isAssignable(TopType to, BottomType from) {
			return true;
		}
		default boolean isAssignable(IntType to, IntType from) {
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

		default void checkAssignment(Type to, Type from, IRNode node) {
			if (!isAssignable(to, from)) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Incompatible types; expected " + to + " but was " + from + ".", sourceUnit(), node));
			}
		}

		default void checkTypes(IRNode node) {
			node.forEachChild(this::check);
		}

		default void checkTypes(StmtAssignment assignment) {
			checkAssignment(
					types().lvalueType(assignment.getLValue()),
					types().type(assignment.getExpression()),
					assignment);
		}

		default void checkTypes(VarDecl varDecl) {
			if (varDecl.getValue() != null) {
				checkAssignment(
						types().declaredType(varDecl),
						types().type(varDecl.getValue()),
						varDecl);
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

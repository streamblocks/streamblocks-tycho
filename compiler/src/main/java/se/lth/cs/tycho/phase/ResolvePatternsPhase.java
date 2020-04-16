package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.Collections;

public class ResolvePatternsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Resolve patterns of case alternatives";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("typeScopes").to(task.getModule(TypeScopes.key))
				.instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(PatternExpression pattern) {
			Expression expr = pattern.getExpression();
			if (expr instanceof ExprVariable) {
				ExprVariable exprVariable = (ExprVariable) expr;
				if (exprVariable.getVariable().getName().equals("_")) {
					return new PatternWildcard(exprVariable);
				} else if (typeScopes().construction(exprVariable).isPresent()) {
					return new PatternDeconstruction(exprVariable, exprVariable.getVariable().getName(), Collections.emptyList());
				} else {
					PatternVarDecl decl = new PatternVarDecl(exprVariable.getVariable().getName());
					decl.setPosition(exprVariable.getFromLineNumber(), exprVariable.getFromColumnNumber(), exprVariable.getToLineNumber(), exprVariable.getToColumnNumber());
					return new PatternBinding(exprVariable, decl);
				}
			}
			if (expr instanceof ExprApplication && ((ExprApplication) expr).getFunction() instanceof ExprVariable) {
				ExprApplication application = (ExprApplication) expr;
				ExprVariable exprVariable = (ExprVariable) application.getFunction();
				if (typeScopes().construction(exprVariable).isPresent()) {
					return new PatternDeconstruction(application, exprVariable.getVariable().getName(), application.getArgs().stream().map(arg -> apply(new PatternExpression(arg, arg))).map(Pattern.class::cast).collect(ImmutableList.collector()));
				}
			}
			return pattern;
		}
	}
}

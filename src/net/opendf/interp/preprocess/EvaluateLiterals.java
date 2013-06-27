package net.opendf.interp.preprocess;

import net.opendf.interp.values.ConstRef;
import net.opendf.interp.values.RefView;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Transition;
import net.opendf.ir.am.util.ActorMachineUtils;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.transformers.AbstractTraverser;

public class EvaluateLiterals {
	private final LitEvaluator evaluator;

	public EvaluateLiterals() {
		evaluator = new LitEvaluator();
	}

	public void evaluateLiterals(ActorMachine actorMachine) {
		for (Scope s : actorMachine.getScopes()) {
			for (Decl d : s.getDeclarations()) {
				evaluator.traverseDecl(d, null);
			}
		}
		for (Transition t : ActorMachineUtils.collectTransitions(actorMachine)) {
			evaluator.traverseStatements(t.getBody(), null);
		}
		for (Expression c : ActorMachineUtils.collectPredicateConditionExpressions(actorMachine)) {
			evaluator.traverseExpression(c, null);
		}
	}

	private static class LitEvaluator extends AbstractTraverser<Void> {
		@Override
		public Void visitExprLiteral(ExprLiteral lit, Void v) {
			RefView r;
			switch (lit.getKind()) {
			case ExprLiteral.litFalse:
				r = ConstRef.of(0);
				break;
			case ExprLiteral.litTrue:
				r = ConstRef.of(1);
				break;
			case ExprLiteral.litInteger:
				r = ConstRef.of(Long.parseLong(lit.getText()));
				break;
			case ExprLiteral.litReal:
				r = ConstRef.of(Double.parseDouble(lit.getText()));
				break;
			default:
				r = null;
				System.out.println("Literal kind " + lit.getKind() + " not supported.");
			}
			lit.setValue(r);
			return super.visitExprLiteral(lit, null);
		}
	}

}

package se.lth.cs.tycho.analysis.value;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.analysis.name.NameAnalysis;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.messages.util.Result;

public class ConstantEvaluation extends Module<ConstantEvaluation.Attributes> {

	public interface Attributes extends Declarations, NameAnalysis.Declarations {
	}

	public interface Declarations {
		@Synthesized
		Optional<Object> constant(Expression expr);
	}

	public Optional<Object> constant(Expression expr) {
		return Optional.empty();
	}

	public Optional<Object> constant(ExprLiteral lit) {
		switch (lit.getKind()) {
		case Integer:
			String text = lit.getText();
			if (text.startsWith("0x") || text.startsWith("0X")) {
				return Optional.of(Integer.parseInt(text.substring(2), 16));
			}
			return Optional.of(Integer.parseInt(lit.getText()));
		case True:
			return Optional.of(true);
		case False:
			return Optional.of(false);
		default:
			return Optional.empty();
		}
	}

	public Optional<Object> constant(ExprVariable var) {
		Result<VarDecl> decl = e().variableDeclaration(var.getVariable());
		if (decl.isSuccess() && decl.get().isConstant() && decl.get().getValue() != null) {
			return e().constant(decl.get().getValue());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<Object> constant(ExprApplication apply) {
		if (apply.getFunction() instanceof ExprVariable) {
			ExprVariable func = (ExprVariable) apply.getFunction();
			BinaryOperator<Integer> binOp = null;
			UnaryOperator<Integer> unOp = null;
			switch (func.getVariable().getName()) {
			case "$BinaryOperation.+":
				binOp = (a, b) -> a + b;
				break;
			case "$BinaryOperation.-":
				binOp = (a, b) -> a - b;
				break;
			case "$BinaryOperation.*":
				binOp = (a, b) -> a * b;
				break;
			case "$BinaryOperation./":
				binOp = (a, b) -> a / b;
				break;
			case "$UnaryOperation.-":
				unOp = a -> -a;
				break;
			}
			if (binOp != null) {
				Optional<Object> left = e().constant(apply.getArgs().get(0));
				Optional<Object> right = e().constant(apply.getArgs().get(1));
				if (left.isPresent() && left.get() instanceof Integer && right.isPresent() && right.get() instanceof Integer) {
					return Optional.of(binOp.apply((Integer) left.get(), (Integer) right.get()));
				}
			}
			if (unOp != null) {
				Optional<Object> val = e().constant(apply.getArgs().get(0));
				if (val.isPresent() && val.get() instanceof Integer) {
					Integer v = (Integer) val.get();
					return Optional.of(unOp.apply(v));
				}
			}
		}
		return Optional.empty();
	}

}

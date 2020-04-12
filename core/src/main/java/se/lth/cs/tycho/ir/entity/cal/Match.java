package se.lth.cs.tycho.ir.entity.cal;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.expr.ExprCase;

import java.util.Objects;
import java.util.function.Consumer;

public class Match extends AbstractIRNode {

	private InputVarDecl declaration;
	private ExprCase expression;

	public Match(InputVarDecl declaration, ExprCase expression) {
		this(null, declaration, expression);
	}

	public Match(IRNode original, InputVarDecl declaration, ExprCase expression) {
		super(original);
		this.declaration = declaration;
		this.expression = expression;
	}

	public Match copy(InputVarDecl declaration, ExprCase expression) {
		if (Objects.equals(getDeclaration(), declaration) && Objects.equals(getExpression(), expression)) {
			return this;
		} else {
			return new Match(this, declaration, expression);
		}
	}

	public InputVarDecl getDeclaration() {
		return declaration;
	}

	public ExprCase getExpression() {
		return expression;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getDeclaration());
		action.accept(getExpression());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy((InputVarDecl) transformation.apply(getDeclaration()), (ExprCase) transformation.apply(getExpression()));
	}
}

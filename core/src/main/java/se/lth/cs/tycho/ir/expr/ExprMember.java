package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.module.ModuleExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class ExprMember extends Expression {
	private ModuleExpr module;
	private String member;

	public ExprMember(ModuleExpr module, String member) {
		this(null, module, member);
	}

	private ExprMember(ExprMember original, ModuleExpr module, String member) {
		super(original);
		this.module = module;
		this.member = member;
	}

	public ExprMember copy(ModuleExpr module, String member) {
		if (Objects.equals(this.module, module) && Objects.equals(this.member, member)) {
			return this;
		}
		return new ExprMember(this, module, member);
	}

	public ModuleExpr getStructure() {
		return module;
	}

	public String getMember() {
		return member;
	}

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprMember(this, p);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(module);
	}

	@Override
	public ExprMember transformChildren(Transformation transformation) {
		return copy((ModuleExpr) transformation.apply(module), member);
	}
}

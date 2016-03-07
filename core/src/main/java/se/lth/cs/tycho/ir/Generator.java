package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class Generator extends AbstractIRNode {
	private final ImmutableList<VarDecl> varDecls;
	private final Expression collection;

	public Generator(List<VarDecl> varDecls, Expression collection) {
		this(null, varDecls, collection);
	}

	private Generator(IRNode original, List<VarDecl> varDecls, Expression collection) {
		super(original);
		this.varDecls = ImmutableList.from(varDecls);
		this.collection = collection;
	}

	public Generator copy(List<VarDecl> varDecls, Expression collection) {
		if (Lists.sameElements(this.varDecls, varDecls) && this.collection == collection) {
			return this;
		} else {
			return new Generator(this, varDecls, collection);
		}
	}

	public ImmutableList<VarDecl> getVarDecls() {
		return varDecls;
	}

	public Generator withVarDecls(List<VarDecl> varDecls) {
		return copy(varDecls, collection);
	}

	public Expression getCollection() {
		return collection;
	}

	public Generator withCollection(Expression collection) {
		return copy(varDecls, collection);
	}

	public Generator clone() {
		return (Generator) super.clone();
	}

	public Generator deepClone() {
		return (Generator) super.deepClone();
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		varDecls.forEach(action);
		action.accept(collection);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				transformation.mapChecked(VarDecl.class, varDecls),
				transformation.applyChecked(Expression. class, collection)
		);
	}
}

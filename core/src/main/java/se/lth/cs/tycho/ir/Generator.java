package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.decl.GeneratorVarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class Generator extends AbstractIRNode {
	private final TypeExpr type;
	private final ImmutableList<GeneratorVarDecl> varDecls;
	private final Expression collection;

	public Generator(TypeExpr type, List<GeneratorVarDecl> varDecls, Expression collection) {
		this(null, type, varDecls, collection);
	}

	private Generator(IRNode original, TypeExpr type, List<GeneratorVarDecl> varDecls, Expression collection) {
		super(original);
		this.type = type;
		this.varDecls = ImmutableList.from(varDecls);
		this.collection = collection;
	}

	public Generator copy(TypeExpr type, List<GeneratorVarDecl> varDecls, Expression collection) {
		if (this.type == type && Lists.sameElements(this.varDecls, varDecls) && this.collection == collection) {
			return this;
		} else {
			return new Generator(this, type, varDecls, collection);
		}
	}

	public TypeExpr getType() {
		return type;
	}

	public Generator withType(TypeExpr type) {
		return copy(type, varDecls, collection);
	}

	public ImmutableList<GeneratorVarDecl> getVarDecls() {
		return varDecls;
	}

	public Generator withVarDecls(List<GeneratorVarDecl> varDecls) {
		return copy(type, varDecls, collection);
	}

	public Expression getCollection() {
		return collection;
	}

	public Generator withCollection(Expression collection) {
		return copy(type, varDecls, collection);
	}

	public Generator clone() {
		return (Generator) super.clone();
	}

	public Generator deepClone() {
		return (Generator) super.deepClone();
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (type != null) action.accept(type);
		varDecls.forEach(action);
		action.accept(collection);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				type == null ? null : transformation.applyChecked(TypeExpr.class, type),
				transformation.mapChecked(GeneratorVarDecl.class, varDecls),
				transformation.applyChecked(Expression. class, collection)
		);
	}
}

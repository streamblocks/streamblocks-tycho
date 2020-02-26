package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProductTypeDecl extends AlgebraicTypeDecl {

	private List<FieldDecl> fields;

	public ProductTypeDecl(String name, List<FieldDecl> fields) {
		this(null, name, fields);
	}

	public ProductTypeDecl(AbstractDecl original, String name, List<FieldDecl> fields) {
		super(original, name);
		this.fields = ImmutableList.from(fields);
	}

	public List<FieldDecl> getFields() {
		return fields;
	}

	@Override
	public Decl withName(String name) {
		return copy(name, getFields());
	}

	private ProductTypeDecl copy(String name, List<FieldDecl> fields) {
		if (Objects.equals(getName(), name) && Lists.sameElements(getFields(), fields)) {
			return this;
		} else {
			return new ProductTypeDecl(this, name, fields);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getFields().forEach(action);
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(getName(), transformation.mapChecked(FieldDecl.class, getFields()));
	}
}

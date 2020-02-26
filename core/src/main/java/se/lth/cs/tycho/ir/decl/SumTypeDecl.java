package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SumTypeDecl extends AlgebraicTypeDecl {

	public static class VariantDecl extends AbstractDecl {

		private ImmutableList<FieldDecl> fields;

		public VariantDecl(String name, List<FieldDecl> fields) {
			this(null, name, fields);
		}

		private VariantDecl(AbstractDecl original, String name, List<FieldDecl> fields) {
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

		private VariantDecl copy(String name, List<FieldDecl> fields) {
			if (Objects.equals(getName(), name) && Lists.sameElements(getFields(), fields)) {
				return this;
			} else {
				return new VariantDecl(this, name, fields);
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

	private ImmutableList<VariantDecl> variants;

	public SumTypeDecl(String name, List<VariantDecl> variants) {
		this(null, name, variants);
	}

	private SumTypeDecl(AbstractDecl original, String name, List<VariantDecl> variants) {
		super(original, name);
		this.variants = ImmutableList.from(variants);
	}

	public ImmutableList<VariantDecl> getVariants() {
		return variants;
	}

	public SumTypeDecl copy(String name, List<VariantDecl> types) {
		if (Objects.equals(getName(), name) && Lists.sameElements(getVariants(), types)) {
			return this;
		} else {
			return new SumTypeDecl(this, name, types);
		}
	}

	@Override
	public Decl withName(String name) {
		return copy(name, getVariants());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		variants.forEach(action);
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(getName(), transformation.mapChecked(VariantDecl.class, variants));
	}
}

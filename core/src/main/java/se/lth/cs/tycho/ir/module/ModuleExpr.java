package se.lth.cs.tycho.ir.module;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ModuleExpr extends AbstractIRNode {
	private final String name;
	private final ImmutableList<TypeParameter> typeParameters;
	private final ImmutableList<ValueParameter> valueParameters;

	public ModuleExpr(String name, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters) {
		this(null, name, typeParameters, valueParameters);
	}

	private ModuleExpr(IRNode original, String name, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters) {
		super(original);
		this.name = name;
		this.typeParameters = ImmutableList.from(typeParameters);
		this.valueParameters = ImmutableList.from(valueParameters);
	}

	public ModuleExpr copy(String name, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters) {
		if (Objects.equals(this.name, name) && Lists.sameElements(this.typeParameters, valueParameters) && Lists.sameElements(this.valueParameters, valueParameters)) {
			return this;
		} else {
			return new ModuleExpr(this, name, typeParameters, valueParameters);
		}
	}

	public String getName() {
		return name;
	}

	public ModuleExpr withName(String name) {
		return copy(name, typeParameters, valueParameters);
	}

	public ImmutableList<TypeParameter> getTypeParameters() {
		return typeParameters;
	}

	public ModuleExpr withTypeParameters(List<TypeParameter> typeParameters) {
		return copy(name, typeParameters, valueParameters);
	}

	public ImmutableList<ValueParameter> getValueParameters() {
		return valueParameters;
	}

	public ModuleExpr withValueParameters(List<ValueParameter> valueParameters) {
		return copy(name, typeParameters, valueParameters);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		typeParameters.forEach(action);
		valueParameters.forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(name, (List) typeParameters.map(transformation), (List) valueParameters.map(transformation));
	}
}

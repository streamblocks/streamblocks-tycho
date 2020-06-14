package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ExprTypeConstruction extends Expression {

	private String constructor;
	private ImmutableList<TypeParameter> typeParameters;
	private ImmutableList<ValueParameter> valueParameters;
	private ImmutableList<Expression> args;

	public ExprTypeConstruction(String constructor, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters, List<Expression> args) {
		this(null, constructor, typeParameters, valueParameters, args);
	}

	public ExprTypeConstruction(IRNode original, String constructor, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters, List<Expression> args) {
		super(original);
		this.constructor = constructor;
		this.typeParameters = ImmutableList.from(typeParameters);
		this.valueParameters = ImmutableList.from(valueParameters);
		this.args = ImmutableList.from(args);
	}

	public String getConstructor() {
		return constructor;
	}

	public ImmutableList<TypeParameter> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ValueParameter> getValueParameters() {
		return valueParameters;
	}

	public ImmutableList<Expression> getArgs() {
		return args;
	}

	public ExprTypeConstruction withConstructor(String constructor) {
		return copy(constructor, getTypeParameters(), getValueParameters(), getArgs());
	}

	public ExprTypeConstruction withTypeParameters(ImmutableList<TypeParameter> typeParameters) {
		return copy(getConstructor(), typeParameters, getValueParameters(), getArgs());
	}

	public ExprTypeConstruction withValueParameters(ImmutableList<ValueParameter> valueParameters) {
		return copy(getConstructor(), getTypeParameters(), valueParameters, getArgs());
	}

	public ExprTypeConstruction copy(String constructor, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters, List<Expression> args) {
		if (Objects.equals(constructor, getConstructor()) && Lists.sameElements(getTypeParameters(), typeParameters) && Lists.sameElements(getValueParameters(), valueParameters) && Lists.sameElements(args, getArgs())) {
			return this;
		} else {
			return new ExprTypeConstruction(this, constructor, typeParameters, valueParameters, args);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getArgs().forEach(action);
		getTypeParameters().forEach(action);
		getValueParameters().forEach(action);
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy(getConstructor(), transformation.mapChecked(TypeParameter.class, getTypeParameters()), transformation.mapChecked(ValueParameter.class, getValueParameters()), (List) getArgs().map(transformation));
	}

	@Override
	public ExprTypeConstruction clone() {
		return (ExprTypeConstruction) super.clone();
	}

	@Override
	public ExprTypeConstruction deepClone() {
		return (ExprTypeConstruction) super.deepClone();
	}
}

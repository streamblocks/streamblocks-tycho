package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PatternDeconstruction extends Pattern {

	private String deconstructor;
	private ImmutableList<TypeParameter> typeParameters;
	private ImmutableList<ValueParameter> valueParameters;
	private ImmutableList<Pattern> patterns;

	public PatternDeconstruction(String deconstructor, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters, List<Pattern> patterns) {
		this(null, deconstructor, typeParameters, valueParameters, patterns);
	}

	public PatternDeconstruction(IRNode original, String deconstructor, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters, List<Pattern> patterns) {
		super(original);
		this.deconstructor = deconstructor;
		this.typeParameters = ImmutableList.from(typeParameters);
		this.valueParameters = ImmutableList.from(valueParameters);
		this.patterns = ImmutableList.from(patterns);
	}

	public String getDeconstructor() {
		return deconstructor;
	}

	public ImmutableList<TypeParameter> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ValueParameter> getValueParameters() {
		return valueParameters;
	}

	public ImmutableList<Pattern> getPatterns() {
		return patterns;
	}

	public PatternDeconstruction withDeconstructor(String deconstructor) {
		return copy(deconstructor, getTypeParameters(), getValueParameters(), getPatterns());
	}

	public PatternDeconstruction withTypeParameters(ImmutableList<TypeParameter> typeParameters) {
		return copy(getDeconstructor(), typeParameters, getValueParameters(), getPatterns());
	}

	public PatternDeconstruction withValueParameters(ImmutableList<ValueParameter> valueParameters) {
		return copy(getDeconstructor(), getTypeParameters(), valueParameters, getPatterns());
	}

	public PatternDeconstruction copy(String deconstructor, List<TypeParameter> typeParameters, List<ValueParameter> valueParameters, List<Pattern> patterns) {
		if (Objects.equals(getDeconstructor(), deconstructor) && Lists.sameElements(getTypeParameters(), typeParameters) && Lists.sameElements(getValueParameters(), valueParameters) && Lists.sameElements(getPatterns(), patterns)) {
			return this;
		} else {
			return new PatternDeconstruction(this, deconstructor, typeParameters, valueParameters, patterns);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getPatterns().forEach(action);
		getTypeParameters().forEach(action);
		getValueParameters().forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(getDeconstructor(), transformation.mapChecked(TypeParameter.class, getTypeParameters()), transformation.mapChecked(ValueParameter.class, getValueParameters()), transformation.mapChecked(Pattern.class, getPatterns()));
	}

	@Override
	public PatternDeconstruction clone() {
		return (PatternDeconstruction) super.clone();
	}

	@Override
	public PatternDeconstruction deepClone() {
		return (PatternDeconstruction) super.deepClone();
	}
}

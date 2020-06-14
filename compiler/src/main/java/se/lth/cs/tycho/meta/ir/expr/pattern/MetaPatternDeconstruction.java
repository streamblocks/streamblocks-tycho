package se.lth.cs.tycho.meta.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.meta.core.MetaArgument;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MetaPatternDeconstruction extends MetaPattern {

	private final PatternDeconstruction patternDeconstruction;

	public MetaPatternDeconstruction(List<MetaArgument> arguments, PatternDeconstruction patternDeconstruction) {
		super(arguments);
		this.patternDeconstruction = patternDeconstruction;
	}

	public MetaPatternDeconstruction copy(List<MetaArgument> arguments, PatternDeconstruction patternDeconstruction) {
		if (Lists.sameElements(getArguments(), arguments) && Objects.equals(getPatternDeconstruction(), patternDeconstruction)) {
			return this;
		} else {
			return new MetaPatternDeconstruction(arguments, patternDeconstruction);
		}
	}

	public PatternDeconstruction getPatternDeconstruction() {
		return patternDeconstruction;
	}

	public MetaPatternDeconstruction withArguments(List<MetaArgument> arguments) {
		return copy(arguments, getPatternDeconstruction());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getPatternDeconstruction());
		getArguments().forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(MetaArgument.class, getArguments()), transformation.applyChecked(PatternDeconstruction.class, getPatternDeconstruction()));
	}
}

package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class PatternTuple extends Pattern {

	private ImmutableList<Pattern> patterns;

	public PatternTuple(List<Pattern> patterns) {
		this(null, patterns);
	}

	public PatternTuple(IRNode original, List<Pattern> patterns) {
		super(original);
		this.patterns = ImmutableList.from(patterns);
	}

	public ImmutableList<Pattern> getPatterns() {
		return patterns;
	}

	public PatternTuple copy(List<Pattern> patterns) {
		if (Lists.sameElements(getPatterns(), patterns)) {
			return this;
		} else {
			return new PatternTuple(this, patterns);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getPatterns().forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(Pattern.class, getPatterns()));
	}
}

package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class PatternAlternative extends Pattern {

	private ImmutableList<Pattern> patterns;

	public PatternAlternative(List<Pattern> patterns) {
		this(null, patterns);
	}

	public PatternAlternative(IRNode original, List<Pattern> patterns) {
		super(original);
		this.patterns = ImmutableList.from(patterns);
	}

	public PatternAlternative copy(List<Pattern> patterns) {
		if (Lists.sameElements(getPatterns(), patterns)) {
			return this;
		} else {
			return new PatternAlternative(this, patterns);
		}
	}

	public ImmutableList<Pattern> getPatterns() {
		return patterns;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		patterns.forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(transformation.mapChecked(Pattern.class, getPatterns()));
	}
}

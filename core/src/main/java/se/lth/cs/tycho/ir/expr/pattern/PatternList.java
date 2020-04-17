package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class PatternList extends Pattern {

	private ImmutableList<Pattern> patterns;

	public PatternList(List<Pattern> patterns) {
		this(null, patterns);
	}

	public PatternList(IRNode original, List<Pattern> patterns) {
		super(original);
		this.patterns = ImmutableList.from(patterns);
	}

	public PatternList copy(List<Pattern> patterns) {
		if (Lists.sameElements(getPatterns(), patterns)) {
			return this;
		} else {
			return new PatternList(this, patterns);
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

package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PatternDeconstructor extends Pattern {

	private String name;
	private ImmutableList<Pattern> patterns;

	public PatternDeconstructor(String name, List<Pattern> patterns) {
		this(null, name, patterns);
	}

	public PatternDeconstructor(IRNode original, String name, List<Pattern> patterns) {
		super(original);
		this.name = name;
		this.patterns = ImmutableList.from(patterns);
	}

	public String getName() {
		return name;
	}

	public ImmutableList<Pattern> getPatterns() {
		return patterns;
	}

	public PatternDeconstructor copy(String name, List<Pattern> patterns) {
		if (Objects.equals(getName(), name) && Lists.sameElements(getPatterns(), patterns)) {
			return this;
		} else {
			return new PatternDeconstructor(this, name, patterns);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getPatterns().forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(getName(), transformation.mapChecked(Pattern.class, getPatterns()));
	}
}

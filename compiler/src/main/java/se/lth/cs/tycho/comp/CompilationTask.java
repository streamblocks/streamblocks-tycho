package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompilationTask implements IRNode {
	private final ImmutableList<SourceUnit> sourceUnits;
	private final QID identifier;

	public CompilationTask(List<SourceUnit> sourceUnits, QID identifier) {
		this.sourceUnits = ImmutableList.from(sourceUnits);
		this.identifier = identifier;
	}

	public CompilationTask copy(List<SourceUnit> sourceUnits, QID identifier) {
		if (Lists.elementIdentityEquals(this.sourceUnits, sourceUnits)
				&& Objects.equals(this.identifier, identifier)) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier);
		}
	}

	public ImmutableList<SourceUnit> getSourceUnits() {
		return sourceUnits;
	}

	public CompilationTask withSourceUnits(List<SourceUnit> sourceUnits) {
		if (Lists.elementIdentityEquals(this.sourceUnits, sourceUnits)) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier);
		}
	}

	public QID getIdentifier() {
		return identifier;
	}

	public CompilationTask withIdentifier(QID identifier) {
		if (Objects.equals(this.identifier, identifier)) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		sourceUnits.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public CompilationTask transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) sourceUnits.map(transformation),
				identifier
		);
	}

	@Override
	public CompilationTask clone() {
		try {
			return (CompilationTask) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
}

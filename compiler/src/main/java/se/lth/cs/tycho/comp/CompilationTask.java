package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
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
	private final NamespaceDecl target;

	public CompilationTask(List<SourceUnit> sourceUnits, QID identifier, NamespaceDecl target) {
		this.sourceUnits = ImmutableList.from(sourceUnits);
		this.identifier = identifier;
		this.target = target;
	}

	public CompilationTask copy(List<SourceUnit> sourceUnits, QID identifier, NamespaceDecl target) {
		if (Lists.elementIdentityEquals(this.sourceUnits, sourceUnits)
				&& Objects.equals(this.identifier, identifier) && this.target == target) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier, target);
		}
	}

	public ImmutableList<SourceUnit> getSourceUnits() {
		return sourceUnits;
	}

	public CompilationTask withSourceUnits(List<SourceUnit> sourceUnits) {
		if (Lists.elementIdentityEquals(this.sourceUnits, sourceUnits)) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier, target);
		}
	}

	public QID getIdentifier() {
		return identifier;
	}

	public CompilationTask withIdentifier(QID identifier) {
		if (Objects.equals(this.identifier, identifier)) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier, target);
		}
	}

	public NamespaceDecl getTarget() {
		return target;
	}

	public CompilationTask withTarget(NamespaceDecl target) {
		if (this.target == target) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier, target);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		sourceUnits.forEach(action);
		if (target != null) action.accept(target);
	}

	@Override
	public CompilationTask transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) sourceUnits.map(transformation),
				identifier,
				target == null ? null : (NamespaceDecl) transformation.apply(target)
		);
	}

	@Override
	public IRNode clone() {
		try {
			return (IRNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
}

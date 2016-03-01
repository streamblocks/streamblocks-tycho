package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CompilationTask implements IRNode {
	private final ImmutableList<SourceUnit> sourceUnits;
	private final QID identifier;
	private final Network network;

	public CompilationTask(List<SourceUnit> sourceUnits, QID identifier, Network network) {
		this.sourceUnits = ImmutableList.from(sourceUnits);
		this.identifier = identifier;
		this.network = network;
	}

	public CompilationTask copy(List<SourceUnit> sourceUnits, QID identifier, Network network) {
		if (Lists.sameElements(this.sourceUnits, sourceUnits)
				&& Objects.equals(this.identifier, identifier)
				&& this.network == network) {
			return this;
		} else {
			return new CompilationTask(sourceUnits, identifier, network);
		}
	}

	public ImmutableList<SourceUnit> getSourceUnits() {
		return sourceUnits;
	}

	public CompilationTask withSourceUnits(List<SourceUnit> sourceUnits) {
		return copy(sourceUnits, identifier, network);
	}

	public QID getIdentifier() {
		return identifier;
	}

	public CompilationTask withIdentifier(QID identifier) {
		return copy(sourceUnits, identifier, network);
	}

	public Network getNetwork() {
		return network;
	}

	public CompilationTask withNetwork(Network network) {
		return copy(sourceUnits, identifier, network);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		sourceUnits.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public CompilationTask transformChildren(Transformation transformation) {
		return copy(
				transformation.mapChecked(SourceUnit.class, sourceUnits),
				identifier,
				network == null ? null : transformation.applyChecked(Network.class, network)
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

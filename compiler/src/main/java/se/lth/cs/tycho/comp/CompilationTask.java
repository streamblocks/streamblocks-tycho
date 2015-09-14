package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompilationTask implements IRNode {
	private final List<SourceUnit> sourceUnits;
	private final QID identifier;

	public CompilationTask(List<SourceUnit> sourceUnits, QID identifier) {
		this.sourceUnits = sourceUnits;
		this.identifier = identifier;
	}

	public List<SourceUnit> getSourceUnits() {
		return sourceUnits;
	}

	public CompilationTask withSourceUnits(List<SourceUnit> sourceUnits) {
		return new CompilationTask(sourceUnits, identifier);
	}

	public QID getIdentifier() {
		return identifier;
	}

	public CompilationTask withIdentifier(QID identifier) {
		return new CompilationTask(sourceUnits, identifier);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		sourceUnits.forEach(action);
	}

	@Override
	public IRNode transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return withSourceUnits(sourceUnits.stream().map(unit -> (SourceUnit) transformation.apply(unit)).collect(Collectors.toList()));
	}
}

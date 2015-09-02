package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

public class CompilationUnit implements IRNode {
	private final List<SourceUnit> sourceUnits;
	private final QID identifier;

	public CompilationUnit(List<SourceUnit> sourceUnits, QID identifier) {
		this.sourceUnits = sourceUnits;
		this.identifier = identifier;
	}

	public List<SourceUnit> getSourceUnits() {
		return sourceUnits;
	}

	public CompilationUnit withSourceUnits(List<SourceUnit> sourceUnits) {
		return new CompilationUnit(sourceUnits, identifier);
	}

	public QID getIdentifier() {
		return identifier;
	}

	public CompilationUnit withIdentifier(QID identifier) {
		return new CompilationUnit(sourceUnits, identifier);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		sourceUnits.forEach(action);
	}
}

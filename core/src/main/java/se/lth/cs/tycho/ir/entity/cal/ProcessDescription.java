package se.lth.cs.tycho.ir.entity.cal;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProcessDescription extends AbstractIRNode {
	private final ImmutableList<Statement> statements;
	private final boolean repeated;

	public ProcessDescription(List<Statement> statements, boolean repeated) {
		this(null, ImmutableList.from(statements), repeated);
	}

	private ProcessDescription(IRNode original, ImmutableList<Statement> statements, boolean repeated) {
		super(original);
		this.statements = statements;
		this.repeated = repeated;
	}

	public ProcessDescription copy(List<Statement> statements, boolean repeated) {
		if (Lists.elementIdentityEquals(this.statements, statements) && this.repeated == repeated) {
			return this;
		} else {
			return new ProcessDescription(this, ImmutableList.from(statements), repeated);
		}
	}

	public ImmutableList<Statement> getStatements() {
		return statements;
	}

	public boolean isRepeated() {
		return repeated;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		statements.forEach(action);
	}

	@Override
	public ProcessDescription transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) statements.map(transformation),
				repeated
		);
	}
}

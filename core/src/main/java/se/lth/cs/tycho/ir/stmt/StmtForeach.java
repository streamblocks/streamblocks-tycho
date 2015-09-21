package se.lth.cs.tycho.ir.stmt;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class StmtForeach extends Statement {

	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtForeach(this, p);
	}

	public StmtForeach(ImmutableList<GeneratorFilter> generators, Statement body) {
		this(null, generators, body);
	}
	
	private StmtForeach(StmtForeach original, ImmutableList<GeneratorFilter> generators, Statement body) {
		super(original);
		this.generators = ImmutableList.copyOf(generators);
		this.body = body;
	}

	public StmtForeach copy(ImmutableList<GeneratorFilter> generators, Statement body) {
		if (Lists.equals(this.generators, generators) && Objects.equals(this.body, body)) {
			return this;
		}
		return new StmtForeach(this, generators, body);
	}

	public ImmutableList<GeneratorFilter> getGenerators() {
		return generators;
	}

	public Statement getBody() {
		return body;
	}

	private ImmutableList<GeneratorFilter> generators;
	private Statement body;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		generators.forEach(action);
		action.accept(body);
	}

	@Override
	public StmtForeach transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) generators.map(transformation),
				(Statement) transformation.apply(body)
		);
	}
}

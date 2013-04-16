package net.opendf.ir.common;

import net.opendf.ir.util.ImmutableList;

public class StmtForeach extends Statement {

	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtForeach(this, p);
	}

	public StmtForeach(ImmutableList<GeneratorFilter> generators, Statement body) {
		this.generators = ImmutableList.copyOf(generators);
		this.body = body;
	}

	public ImmutableList<GeneratorFilter> getGenerators() {
		return generators;
	}

	public Statement getBody() {
		return body;
	}

	private ImmutableList<GeneratorFilter> generators;
	private Statement body;
}

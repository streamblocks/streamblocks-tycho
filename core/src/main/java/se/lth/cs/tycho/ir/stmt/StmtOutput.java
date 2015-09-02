package se.lth.cs.tycho.ir.stmt;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class StmtOutput extends Statement {

	@Override
	public <R,P> R accept(StatementVisitor<R,P> v, P p) {
		return v.visitStmtOutput(this, p);
	}

	public Port  getPort() { return port; }
	
	public boolean  hasRepeat() { return hasRepeat; }
	
	public int  getRepeat() { return repeat; }
	
	public ImmutableList<Expression>  getValues() { return values; }


	//
	//  Ctor
	//
	
	public StmtOutput(ImmutableList<Expression> values, Port port) {
		this(null, values, port, false, 0);
	}
	
	public StmtOutput(ImmutableList<Expression> values, Port port, int repeat) {
		this(null, values, port, true, repeat);
	}

	private StmtOutput(StmtOutput original, ImmutableList<Expression> values, Port port, boolean hasRepeat, int repeat) {
		super(original);
		this.values = ImmutableList.copyOf(values);
		this.port = port;
		this.hasRepeat = hasRepeat;
		this.repeat = repeat;
	}
	
	public StmtOutput copy(ImmutableList<Expression> values, Port port) {
		if (!hasRepeat && Lists.equals(this.values, values) && Objects.equals(this.port, port)) {
			return this;
		}
		return new StmtOutput(this, values, port, false, 0);
	}
	
	public StmtOutput copy(ImmutableList<Expression> values, Port port, int repeat) {
		if (hasRepeat && Lists.equals(this.values, values) && Objects.equals(this.port, port) && this.repeat == repeat) {
			return this;
		}
		return new StmtOutput(this, values, port, true, repeat);
	}
	
	private ImmutableList<Expression>	values;
	
	private boolean			hasRepeat;
	private Port			port;
	private int				repeat;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
		values.forEach(action);
	}
}

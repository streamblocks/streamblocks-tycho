package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ExprConstruction extends Expression {

	private String type;
	private String constructor;
	private ImmutableList<Expression> args;

	public ExprConstruction(String parent, String name, List<Expression> args) {
		this(null, parent, name, args);
	}

	private ExprConstruction(IRNode original, String parent, String name, List<Expression> args) {
		super(original);
		this.type = parent;
		this.constructor = name;
		this.args = ImmutableList.from(args);
	}

	public String getType() {
		return type;
	}

	public String getConstructor() {
		return constructor;
	}

	public ImmutableList<Expression> getArgs() {
		return args;
	}

	public ExprConstruction copy(String parent, String child, List<Expression> args) {
		if (Objects.equals(parent, getType()) && Objects.equals(child, getConstructor()) && Lists.sameElements(args, getArgs())) {
			return this;
		} else {
			return new ExprConstruction(this, parent, child, args);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		args.forEach(action);
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy(getType(), getConstructor(), (List) getArgs().map(transformation));
	}
}

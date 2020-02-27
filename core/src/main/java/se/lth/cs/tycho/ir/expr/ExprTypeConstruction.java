package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ExprTypeConstruction extends Expression {

	private String constructor;
	private ImmutableList<Expression> args;

	public ExprTypeConstruction(String constructor, List<Expression> args) {
		this(null, constructor, args);
	}

	private ExprTypeConstruction(IRNode original, String constructor, List<Expression> args) {
		super(original);
		this.constructor = constructor;
		this.args = ImmutableList.from(args);
	}

	public String getConstructor() {
		return constructor;
	}

	public ImmutableList<Expression> getArgs() {
		return args;
	}

	public ExprTypeConstruction copy(String type, List<Expression> args) {
		if (Objects.equals(type, getConstructor()) && Lists.sameElements(args, getArgs())) {
			return this;
		} else {
			return new ExprTypeConstruction(this, type, args);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		args.forEach(action);
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy(getConstructor(), (List) getArgs().map(transformation));
	}
}

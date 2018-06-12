package se.lth.cs.tycho.ir;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.expr.Expression;

/**
 * @author Per Andersson
 *
 */

public class ToolValueAttribute extends ToolAttribute {
	public ToolValueAttribute(String name, Expression value){
		super(name);
		this.value = value;
	}

	protected ToolValueAttribute(ToolValueAttribute original, String name, Expression value) {
		super (original, name);
		this.value = value;
	}
	
	public ToolValueAttribute copy(String name, Expression value){
		if(Objects.equals(this.name, name) && Objects.equals(this.value, value)){
			return this;
		}
		return new ToolValueAttribute(this, name, value);
	}

	public Expression getValue(){
		return value;
	}

	@Override
	Kind getKind() {
		return Kind.type;
	}

	private Expression value;

	@Override
	public ToolValueAttribute transformChildren(Transformation transformation) {
		return copy(name, (Expression) transformation.apply(value));
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(value);
	}
}

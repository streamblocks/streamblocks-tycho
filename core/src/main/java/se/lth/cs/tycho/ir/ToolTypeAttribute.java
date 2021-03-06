package se.lth.cs.tycho.ir;
import java.util.Objects;

import se.lth.cs.tycho.ir.type.TypeExpr;

/**
 * @author Per Andersson
 *
 */

public class ToolTypeAttribute extends ToolAttribute {
	public ToolTypeAttribute(String name, TypeExpr type){
		this(null, name, type);
	}
	protected ToolTypeAttribute(ToolTypeAttribute original, String name, TypeExpr type) {
		super (original, name);
		this.type = type;
	}
	
	public ToolTypeAttribute copy(String name, TypeExpr type){
		if(Objects.equals(this.name, name) && Objects.equals(this.type, type)){
			return this;
		}
		return new ToolTypeAttribute(this, name, type);
	}
	
	public TypeExpr getType(){
		return type;
	}

	private TypeExpr type;

	@Override
	Kind getKind() {
		return Kind.type;
	}

	@Override
	public ToolTypeAttribute transformChildren(Transformation transformation) {
		return copy(name, (TypeExpr) transformation.apply(type));
	}
}

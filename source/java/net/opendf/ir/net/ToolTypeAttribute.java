package net.opendf.ir.net;
import java.util.Objects;

import net.opendf.ir.TypeExpr;
import net.opendf.util.PrettyPrint;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
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
	public void print(java.io.PrintStream out){
		out.append(name);
		out.append(" : ");
		new PrettyPrint().print(type);
	}

	private TypeExpr type;

	@Override
	Kind getKind() {
		return Kind.type;
	}
}

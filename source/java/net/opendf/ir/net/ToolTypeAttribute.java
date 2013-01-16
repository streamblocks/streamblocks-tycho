package net.opendf.ir.net;
import net.opendf.ir.common.TypeExpr;
import net.opendf.util.PrettyPrint;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class ToolTypeAttribute extends ToolAttribute {
	public ToolTypeAttribute(String name, TypeExpr type){
		super(name);
		this.type = type;
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

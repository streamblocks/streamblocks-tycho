package net.opendf.ir.net;
import net.opendf.ir.common.Expression;
import net.opendf.util.PrettyPrint;
/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class ToolValueAttribute extends ToolAttribute {
	public ToolValueAttribute(String name, Expression value){
		super(name);
		this.value = value;
	}
	public Expression getValue(){
		return value;
	}
	public void print(java.io.PrintStream out){
		out.append(name);
		out.append(" = ");
		new PrettyPrint().print(value);
	}
	
	@Override
	Kind getKind() {
		return Kind.type;
	}

	private Expression value;
}

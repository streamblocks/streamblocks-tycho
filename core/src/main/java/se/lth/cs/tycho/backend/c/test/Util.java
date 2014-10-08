package se.lth.cs.tycho.backend.c.test;

import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.net.ToolAttribute;
import se.lth.cs.tycho.ir.net.ToolValueAttribute;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class Util {
	public static TypeExpr uintType(int size) {
		return sizedType("uint", size);
	}
	
	public static TypeExpr intType(int size) {
		return sizedType("int", size);
	}
	
	public static TypeExpr boolType() {
		return new TypeExpr("bool", null, null);
	}
	
	private static TypeExpr sizedType(String name, int size) {
		Expression s = new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(size));
		return new TypeExpr(name, null, ImmutableList.of(ImmutableEntry.of("size", s)));
	}

	public static ImmutableList<ToolAttribute> bufferSize(int size) {
		Expression s = new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(size));
		return ImmutableList.<ToolAttribute> of(new ToolValueAttribute("buffer_size", s));
	}

	public static TypeExpr intType() {
		return new TypeExpr("int", null, null);
	}
	
	public static TypeExpr uintType() {
		return new TypeExpr("uint", null, null);
	}

}

package net.opendf.backend.c.test;

import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.expr.ExprLiteral;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.net.ToolValueAttribute;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;

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

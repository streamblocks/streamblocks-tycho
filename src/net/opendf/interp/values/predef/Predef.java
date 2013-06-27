package net.opendf.interp.values.predef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.opendf.interp.values.ConstRef;
import net.opendf.interp.values.RefView;

public class Predef {
	private static Map<String, RefView> predef = new HashMap<String, RefView>();
	static {
		predef.put("+", ConstRef.of(new IntFunctions.Add()));
		predef.put("-", ConstRef.of(new IntFunctions.Sub()));
		predef.put("*", ConstRef.of(new IntFunctions.Mul()));

		predef.put("bitor", ConstRef.of(new IntFunctions.BitOr()));
		predef.put("bitand", ConstRef.of(new IntFunctions.BitAnd()));
		predef.put("rshift", ConstRef.of(new IntFunctions.RightShiftSignExt()));
		predef.put("lshift", ConstRef.of(new IntFunctions.LeftShift()));

		predef.put("<", ConstRef.of(new IntFunctions.LT()));
		predef.put("<=", ConstRef.of(new IntFunctions.LE()));
		predef.put(">", ConstRef.of(new IntFunctions.GT()));
		predef.put(">=", ConstRef.of(new IntFunctions.GE()));
		predef.put("=", ConstRef.of(new IntFunctions.EQ()));
		predef.put("!=", ConstRef.of(new IntFunctions.NE()));

		predef.put("and", ConstRef.of(new BoolFunctions.And()));
		predef.put("or", ConstRef.of(new BoolFunctions.Or()));
		predef.put("not", ConstRef.of(new BoolFunctions.Not()));
		
		predef.put("Integers", ConstRef.of(new CollFunctions.IntegerRange()));
		
		predef = Collections.unmodifiableMap(predef);
	}

	public static Map<String, RefView> predef() {
		return predef;
	}
}

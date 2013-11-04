package net.opendf.interp.values.predef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.opendf.interp.values.ConstRef;
import net.opendf.interp.values.RefView;

public class Predef {

	private static Map<String, RefView> predef = new HashMap<String, RefView>();
	static {
		predef.put("$UnaryOperation.-", ConstRef.of(new IntFunctions.Negate()));
		predef.put("$UnaryOperation.#", ConstRef.of(new CollFunctions.ListSize()));
		
		predef.put("$BinaryOperation.+", ConstRef.of(new IntFunctions.Add()));
		predef.put("$BinaryOperation.-", ConstRef.of(new IntFunctions.Sub()));
		predef.put("$BinaryOperation.*", ConstRef.of(new IntFunctions.Mul()));
		predef.put("$BinaryOperation./", ConstRef.of(new IntFunctions.Div()));
		predef.put("$BinaryOperation.Mod", ConstRef.of(new IntFunctions.Mod()));

		predef.put("$BinaryOperation.bitor", ConstRef.of(new IntFunctions.BitOr()));
		predef.put("$BinaryOperation.bitand", ConstRef.of(new IntFunctions.BitAnd()));
		predef.put("$BinaryOperation.rshift", ConstRef.of(new IntFunctions.RightShiftSignExt()));
		predef.put("$BinaryOperation.lshift", ConstRef.of(new IntFunctions.LeftShift()));

		predef.put("$BinaryOperation.<", ConstRef.of(new IntFunctions.LT()));
		predef.put("$BinaryOperation.<=", ConstRef.of(new IntFunctions.LE()));
		predef.put("$BinaryOperation.>", ConstRef.of(new IntFunctions.GT()));
		predef.put("$BinaryOperation.>=", ConstRef.of(new IntFunctions.GE()));
		predef.put("$BinaryOperation.=", ConstRef.of(new IntFunctions.EQ()));
		predef.put("$BinaryOperation.!=", ConstRef.of(new IntFunctions.NE()));

		predef.put("$BinaryOperation.and", ConstRef.of(new BoolFunctions.And()));
		predef.put("$BinaryOperation.or", ConstRef.of(new BoolFunctions.Or()));
		predef.put("$BinaryOperation.not", ConstRef.of(new BoolFunctions.Not()));
		
		predef.put("$BinaryOperation...", ConstRef.of(new CollFunctions.IntegerRange()));

		predef.put("accumulate", ConstRef.of(new CollFunctions.ListAccumulate()));
		
		predef = Collections.unmodifiableMap(predef);
	}

	public static Map<String, RefView> predef() {
		return predef;
	}
}

package net.opendf.transform.operators;

import java.util.HashMap;

/**
 * This class contains default mappings for binary operations to their priorities.
 * Used by ActorOpTransformer.
 * 
 * @author pera
 *
 */
public class BinOpPriorities extends HashMap<String, Integer>{
	private static final long serialVersionUID = 1L;

	public static BinOpPriorities getDefaultMapper(){
		BinOpPriorities map = new BinOpPriorities();
		map.setDefaultOps();
		return map;
	}
	public static BinOpPriorities getEmptyMapper(){
		BinOpPriorities map = new BinOpPriorities();
		return map;
	}

	void setDefaultOps() {
		clear();
		put("or", 4);
		put("and", 5);
		put("|", 6);
		put("^", 7);
		put("&", 8);
		put("=", 9);
		put("!=", 9);
		put("<", 10);
		put("<=", 10);
		put(">", 10);
		put(">=", 10);
		put("..", 11);
		put("in", 11);
		put("<<", 11);
		put(">>", 11);
		put("+", 12);
		put("-", 12);
		put("div", 13);
		put("mod", 13);
		put("*", 13);
		put("/", 13);
	}
}

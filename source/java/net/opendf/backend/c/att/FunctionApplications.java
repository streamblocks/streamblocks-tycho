package net.opendf.backend.c.att;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.util.Joiner;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.Variable;
import net.opendf.ir.common.expr.ExprApplication;
import net.opendf.ir.common.expr.ExprVariable;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.common.stmt.StmtCall;

public class FunctionApplications extends Module<FunctionApplications.Decls> {
	public interface Decls {
		@Synthesized
		String functionApplication(ExprVariable func, ExprApplication apply);

		@Synthesized
		public String procedureCall(ExprVariable proc, StmtCall call);

		IRNode declaration(Variable var);

		String parenthesizedExpression(Expression e);

		String simpleExpression(Expression e);

		String functionName(IRNode decl);
	}

	private final Map<String, String> binOps;
	private final Map<String, String> preOps;
	private final Map<String, String> postOps;
	private final Joiner comma = new Joiner(", ");

	public FunctionApplications() {
		Map<String, String> binOps = new HashMap<>();
		Map<String, String> preOps = new HashMap<>();
		Map<String, String> postOps = new HashMap<>();

		binOps.put("$BinaryOperation.&", "&");
		binOps.put("$BinaryOperation.|", "|");
		binOps.put("$BinaryOperation.and", "&&");
		binOps.put("$BinaryOperation.or", "||");
		binOps.put("$BinaryOperation.+", "+");
		binOps.put("$BinaryOperation.-", "-");
		binOps.put("$BinaryOperation.*", "*");
		binOps.put("$BinaryOperation./", "/");
		binOps.put("$BinaryOperation.=", "==");
		binOps.put("$BinaryOperation.!=", "!=");
		binOps.put("$BinaryOperation.<", "<");
		binOps.put("$BinaryOperation.<=", "<=");
		binOps.put("$BinaryOperation.>", ">");
		binOps.put("$BinaryOperation.>=", ">=");
		binOps.put("$BinaryOperation.>>", ">>");
		binOps.put("$BinaryOperation.<<", "<<");
		binOps.put("$BinaryOperation.^", "^");
		binOps.put("lshift", "<<");
		binOps.put("rshift", ">>");
		binOps.put("bitor", "|");
		binOps.put("bitand", "&");
		this.binOps = Collections.unmodifiableMap(binOps);

		preOps.put("$UnaryOperation.-", "-");
		preOps.put("$UnaryOperation.not", "!");
		preOps.put("$UnaryOperation.~", "~");
		this.preOps = Collections.unmodifiableMap(preOps);

		this.postOps = Collections.unmodifiableMap(postOps);
	}

	public String functionApplication(ExprVariable func, ExprApplication apply) {
		assert func == apply.getFunction();
		IRNode decl = e().declaration(func.getVariable());
		if (decl == null) {
			int numArgs = apply.getArgs().size();
			String name = func.getVariable().getName();
			if (numArgs == 2 && binOps.containsKey(name)) {
				String op = binOps.get(name);
				Expression left = apply.getArgs().get(0);
				Expression right = apply.getArgs().get(1);
				return e().parenthesizedExpression(left) + " " + op + " " + e().parenthesizedExpression(right);
			} else if (numArgs == 1 && preOps.containsKey(name)) {
				return preOps.get(name) + e().parenthesizedExpression(apply.getArgs().get(0));
			} else if (numArgs == 1 && postOps.containsKey(name)) {
				return postOps.get(name) + e().parenthesizedExpression(apply.getArgs().get(0));
			} else {
				throw new Error();
			}
		} else {
			String name = e().functionName(decl);
			ArrayList<String> args = new ArrayList<>();
			for (Expression arg : apply.getArgs()) {
				args.add(e().simpleExpression(arg));
			}
			return name + "(" + comma.join(args) + ")";
		}
	}

	public String procedureCall(ExprVariable proc, StmtCall call) {
		IRNode decl = e().declaration(proc.getVariable());
		String name = e().functionName(decl);
		ArrayList<String> args = new ArrayList<>();
		for (Expression arg : call.getArgs()) {
			args.add(e().simpleExpression(arg));
		}
		return name + "(" + comma.join(args) + ");\n";
	}

}
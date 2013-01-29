package net.opendf.interp;

import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.List;
import net.opendf.interp.values.Procedure;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StatementVisitor;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.StmtIf;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.common.StmtWhile;

public class StatementExecutor implements StatementVisitor<Void, Environment> {

	private final Interpreter interpreter;
	private final TypeConverter conv;
	private final Stack stack;
	private final GeneratorFilterHelper gen;

	public StatementExecutor(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.conv = TypeConverter.getInstance();
		this.stack = interpreter.getStack();
		this.gen = new GeneratorFilterHelper(interpreter);
	}

	private void execute(Statement stmt, Environment env) {
		stmt.accept(this, env);
	}

	@Override
	public Void visitStmtAssignment(StmtAssignment stmt, Environment env) {
		Ref ref = stmt.isVariableOnStack() ? stack.peek(stmt.getVariablePosition()) : env.getMemory().get(
				stmt.getVariablePosition());
		Expression[] loc = stmt.getLocation();
		if (loc != null) {
			for (int i = 0; i < loc.length - 1; i++) {
				Expression l = loc[i];
				List list = conv.getList(ref);
				list.get(conv.getInt(interpreter.evaluate(l, env)), stack.push());
				ref = stack.pop();
			}
			List list = conv.getList(ref);
			list.set(conv.getInt(interpreter.evaluate(loc[loc.length - 1], env)), interpreter.evaluate(stmt.getVal(), env));
		} else {
			interpreter.evaluate(stmt.getVal(), env).assignTo(ref);
		}
		return null;
	}

	@Override
	public Void visitStmtBlock(StmtBlock stmt, Environment env) {
		int stackAllocs = 0;
		for (DeclType d : stmt.getTypeDecls()) {
			stackAllocs += interpreter.declare(d, env);
		}
		for (DeclVar d : stmt.getVarDecls()) {
			stackAllocs += interpreter.declare(d, env);
		}
		for (Statement s : stmt.getStatements()) {
			execute(s, env);
		}
		stack.remove(stackAllocs);
		return null;
	}

	@Override
	public Void visitStmtIf(StmtIf stmt, Environment env) {
		RefView condRef = interpreter.evaluate(stmt.getCondition(), env);
		boolean cond = conv.getBoolean(condRef);
		if (cond) {
			execute(stmt.getThenBranch(), env);
		} else {
			execute(stmt.getElseBranch(), env);
		}
		return null;
	}

	@Override
	public Void visitStmtCall(StmtCall stmt, Environment env) {
		RefView r = interpreter.evaluate(stmt.getProcedure(), env);
		Procedure p = conv.getProcedure(r);
		Expression[] argExprs = stmt.getArgs();
		for (Expression arg : argExprs) {
			stack.push(interpreter.evaluate(arg, env));
		}
		p.exec(interpreter);
		return null;
	}

	@Override
	public Void visitStmtOutput(StmtOutput stmt, Environment env) {
		Channel.InputEnd channel = env.getChannelIn(stmt.getChannelId());
		if (stmt.hasRepeat()) {
			Expression[] exprs = stmt.getValues();
			BasicRef[] values = new BasicRef[exprs.length];
			for (int i = 0; i < exprs.length; i++) {
				values[i] = new BasicRef();
				interpreter.evaluate(exprs[i], env).assignTo(values[i]);
			}
			for (int r = 0; r < stmt.getRepeat(); r++) {
				for (BasicRef v : values)
					channel.write(v);
			}
		} else {
			Expression[] exprs = stmt.getValues();
			for (int i = 0; i < exprs.length; i++) {
				channel.write(interpreter.evaluate(exprs[i], env));
			}
		}
		return null;
	}

	@Override
	public Void visitStmtWhile(StmtWhile stmt, Environment env) {
		while (conv.getBoolean(interpreter.evaluate(stmt.getCondition(), env))) {
			execute(stmt.getBody(), env);
		}
		return null;
	}

	@Override
	public Void visitStmtForeach(final StmtForeach stmt, final Environment env) {
		Runnable execStmt = new Runnable() {
			public void run() {
				execute(stmt.getBody(), env);
			}
		};
		gen.generate(stmt.getGenerators(), execStmt, env);
		return null;
	}

}

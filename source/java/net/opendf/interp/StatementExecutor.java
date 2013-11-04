package net.opendf.interp;

import net.opendf.interp.exception.CALRuntimeException;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.List;
import net.opendf.interp.values.Procedure;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.LValueField;
import net.opendf.ir.common.LValueIndexer;
import net.opendf.ir.common.LValueVariable;
import net.opendf.ir.common.LValueVisitor;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StatementVisitor;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.StmtConsume;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.StmtIf;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.common.StmtWhile;
import net.opendf.ir.util.ImmutableList;

public class StatementExecutor implements StatementVisitor<Void, Environment>, LValueVisitor<Ref, Environment> {

	private final Interpreter interpreter;
	private final TypeConverter conv;
	private final Stack stack;
	private final GeneratorFilterHelper gen;
	private final TypeConverter converter;

	public StatementExecutor(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.conv = TypeConverter.getInstance();
		this.stack = interpreter.getStack();
		this.gen = new GeneratorFilterHelper(interpreter);
		this.converter = TypeConverter.getInstance();
	}

	private void execute(Statement stmt, Environment env) throws CALRuntimeException {
		stmt.accept(this, env);
	}


	@Override
	public Void visitStmtAssignment(StmtAssignment stmt, Environment env) throws CALRuntimeException {
		try{
			RefView value = interpreter.evaluate(stmt.getExpression(), env);
			Ref memCell = stmt.getLValue().accept(this, env);
			value.assignTo(memCell);
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}

	/**
	 * Return the memory location of the lhs
	 */
	@Override
	public Ref visitLValueVariable(LValueVariable lvalue, Environment env) {
		VariableLocation var = (VariableLocation)lvalue.getVariable();
		if(var.isScopeVariable()){
			return env.getMemory().get(var);
		} else {
			return stack.peek(var.getOffset());
		}
	}

	/**
	 * Return the memory location of the lhs
	 */
	@Override
	public Ref visitLValueIndexer(LValueIndexer lvalue, Environment env) {
		RefView index = interpreter.evaluate(lvalue.getIndex(), env);
		int i = converter.getInt(index);
		Ref structure = lvalue.getStructure().accept(this, env);
		List l = converter.getList(structure);
		return l.getRef(i);
	}

	/**
	 * Return the memory location of the lhs
	 */
	@Override
	public Ref visitLValueField(LValueField lvalue, Environment env) throws CALRuntimeException {
		// TODO Auto-generated method stub
		throw new CALRuntimeException("unsopported operation, field at lhs of assignment", lvalue);
	}

	@Override
	public Void visitStmtConsume(StmtConsume s, Environment p) throws CALRuntimeException {
		try{
			Channel.OutputEnd source = p.getSourceChannelOutputEnd(s.getPort().getOffset());
			source.remove(s.getNumberOfTokens());
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(s);
			throw e;
		}
	}

	@Override
	public Void visitStmtBlock(StmtBlock stmt, Environment env) throws CALRuntimeException {
		try{
			if(!stmt.getTypeDecls().isEmpty()) {
				throw new UnsupportedOperationException();
			}
			for (DeclVar d : stmt.getVarDecls()) {
				if(d.getInitialValue() != null){
					stack.push(interpreter.evaluate(d.getInitialValue(), env));				
				} else {
					stack.push();
				}
			}
			for (Statement s : stmt.getStatements()) {
				execute(s, env);
			}
			stack.remove(stmt.getVarDecls().size());
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}

	@Override
	public Void visitStmtIf(StmtIf stmt, Environment env) throws CALRuntimeException {
		try{
			//--- condition
			RefView condRef = interpreter.evaluate(stmt.getCondition(), env);
			boolean cond = conv.getBoolean(condRef);
			if (cond) {
				//--- true branch
				execute(stmt.getThenBranch(), env);
			} else {
				execute(stmt.getElseBranch(), env);
			}
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}

	@Override
	public Void visitStmtCall(StmtCall stmt, Environment env) throws CALRuntimeException {
		try{
			RefView r = interpreter.evaluate(stmt.getProcedure(), env);
			Procedure p = conv.getProcedure(r);
			ImmutableList<Expression> argExprs = stmt.getArgs();
			for (Expression arg : argExprs) {
				stack.push(interpreter.evaluate(arg, env));
			}
			p.exec(interpreter);
			stack.remove(stmt.getArgs().size());
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}

	@Override
	public Void visitStmtOutput(StmtOutput stmt, Environment env) throws CALRuntimeException {
		try{
			Channel.InputEnd channel = env.getSinkChannelInputEnd(stmt.getPort().getOffset());
			if (stmt.hasRepeat()) {
				ImmutableList<Expression> exprList = stmt.getValues();
				BasicRef[] values = new BasicRef[exprList.size()];
				for (int i = 0; i < exprList.size(); i++) {
					values[i] = new BasicRef();
					interpreter.evaluate(exprList.get(i), env).assignTo(values[i]);
				}
				for (int r = 0; r < stmt.getRepeat(); r++) {
					for (BasicRef v : values)
						channel.write(v);
				}
			} else {
				ImmutableList<Expression> exprList = stmt.getValues();
				for (Expression expr : exprList) {
					// write() always succeeds so no exceptions to catch
					channel.write(interpreter.evaluate(expr, env));
				}
			}
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}

	@Override
	public Void visitStmtWhile(StmtWhile stmt, Environment env) throws CALRuntimeException {
		try{
			while (conv.getBoolean(interpreter.evaluate(stmt.getCondition(), env))) {
				execute(stmt.getBody(), env);
			}
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}

	@Override
	public Void visitStmtForeach(final StmtForeach stmt, final Environment env) throws CALRuntimeException {
		try{
			Runnable execStmt = new Runnable() {
				public void run() {
					execute(stmt.getBody(), env);
				}
			};
			gen.interpret(stmt.getGenerators(), execStmt, env);
			return null;
		} catch(CALRuntimeException e){
			e.pushCalStack(stmt);
			throw e;
		}
	}
}
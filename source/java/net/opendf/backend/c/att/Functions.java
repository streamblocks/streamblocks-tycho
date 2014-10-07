package net.opendf.backend.c.att;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javarag.Collected;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.CollectionBuilder;
import javarag.coll.Collector;
import net.opendf.backend.c.CType;
import net.opendf.backend.c.util.Joiner;
import net.opendf.ir.IRNode;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.decl.VarDecl;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.expr.ExprLambda;
import net.opendf.ir.expr.ExprLet;
import net.opendf.ir.expr.ExprProc;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.util.ImmutableList;

public class Functions extends Module<Functions.Decls> {
	private final Joiner pars = new Joiner("(", ", ", ")");

	public interface Decls {
		@Synthesized
		String scopeVarInit(Expression expr, VarDecl varDecl);

		@Synthesized
		String lambdaSignature(ExprLambda lambda);

		@Synthesized
		String procedureSignature(ExprProc proc);

		@Synthesized
		void functionDeclarations(ActorMachine actorMachine, PrintWriter writer);

		@Synthesized
		void functionBody(ExprLet let, PrintWriter writer);

		@Synthesized
		void functionDefinitions(ActorMachine actorMachine, PrintWriter writer);

		@Collected
		Set<ExprLambda> lambdaExpressions(ActorMachine am);

		@Collected
		Set<ExprProc> procedureExpressions(ActorMachine am);

		ActorMachine actorMachine(IRNode node);

		String functionName(ExprLambda lambda);

		String functionName(ExprProc proc);

		String variableName(ParDeclValue par);

		String variableName(VarDecl decl);

		CType ctype(TypeExpr type);

		String simpleExpression(Expression e);

		void functionBody(Expression e, PrintWriter w);

		String blockified(Statement s);
	}

	public String scopeVarInit(ExprLambda lambda, VarDecl varDecl) {
		return "";
	}

	public String scopeVarInit(ExprProc procedure, VarDecl varDecl) {
		return "";
	}

	public String lambdaSignature(ExprLambda lambda) {
		String name = e().functionName(lambda);
		List<String> parList = parList(lambda.getValueParameters());
		CType type = e().ctype(lambda.getReturnType());
		return new StringBuilder()
				.append(type.plainType())
				.append(" ")
				.append(name)
				.append(pars.join(parList))
				.toString();
	}

	public String procedureSignature(ExprProc proc) {
		String name = e().functionName(proc);
		List<String> parList = parList(proc.getValueParameters());
		return new StringBuilder()
				.append("void ")
				.append(name)
				.append(pars.join(parList))
				.toString();

	}

	public void functionDeclarations(ActorMachine actorMachine, PrintWriter writer) {
		Collection<ExprLambda> lambdas = e().lambdaExpressions(actorMachine);
		for (ExprLambda lambda : lambdas) {
			writer.print(e().lambdaSignature(lambda));
			writer.println(";");
		}

		Collection<ExprProc> procs = e().procedureExpressions(actorMachine);
		for (ExprProc proc : procs) {
			writer.print(e().procedureSignature(proc));
			writer.println(";");
		}
	}

	public void functionBody(ExprLet let, PrintWriter writer) {
		writer.println("{");
		for (LocalVarDecl decl : let.getVarDecls()) {
			CType type = e().ctype(decl.getType());
			String name = e().variableName(decl);
			String value = e().simpleExpression(decl.getInitialValue());
			writer.print("const ");
			writer.print(type.variableType(name));
			writer.print(" = ");
			writer.print(value);
			writer.println(";");
		}
		String value = e().simpleExpression(let.getBody());
		writer.println("return " + value + ";");
		writer.println("}");
	}

	public void functionDefinitions(ActorMachine actorMachine, PrintWriter writer) {
		Collection<ExprLambda> lambdas = e().lambdaExpressions(actorMachine);
		for (ExprLambda lambda : lambdas) {
			writer.println(e().lambdaSignature(lambda));
			e().functionBody(lambda.getBody(), writer);
		}

		Collection<ExprProc> procs = e().procedureExpressions(actorMachine);
		for (ExprProc proc : procs) {
			writer.println(e().procedureSignature(proc));
			writer.println(e().blockified(proc.getBody()));
		}
	}

	private List<String> parList(ImmutableList<ParDeclValue> pars) {
		List<String> parList = new ArrayList<>();
		for (ParDeclValue par : pars) {
			String name = e().variableName(par);
			CType type = e().ctype(par.getType());
			parList.add(type.variableType(name));
		}
		return parList;
	}

	public Builder<Set<ExprLambda>, ExprLambda> lambdaExpressions(ActorMachine actorMachine) {
		return new CollectionBuilder<Set<ExprLambda>, ExprLambda>(new LinkedHashSet<ExprLambda>());
	}

	public void lambdaExpressions(ExprLambda lambda, Collector<ExprLambda> coll) {
		ActorMachine am = e().actorMachine(lambda);
		coll.add(am, lambda);
	}

	public Builder<Set<ExprProc>, ExprProc> procedureExpressions(ActorMachine actorMachine) {
		return new CollectionBuilder<Set<ExprProc>, ExprProc>(new LinkedHashSet<ExprProc>());
	}

	public void procedureExpressions(ExprProc proc, Collector<ExprProc> coll) {
		ActorMachine am = e().actorMachine(proc);
		coll.add(am, proc);
	}

}
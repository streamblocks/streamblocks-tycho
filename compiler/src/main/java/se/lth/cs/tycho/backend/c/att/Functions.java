package se.lth.cs.tycho.backend.c.att;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import se.lth.cs.tycho.backend.c.CType;
import se.lth.cs.tycho.backend.c.util.Joiner;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import javarag.Collected;
import javarag.Module;
import javarag.Procedural;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.CollectionBuilder;
import javarag.coll.Collector;

public class Functions extends Module<Functions.Decls> {
	private final Joiner pars = new Joiner("(", ", ", ")");

	public interface Decls {
		@Synthesized
		String varInit(Expression expr, String name);

		@Synthesized
		String lambdaSignature(ExprLambda lambda);

		@Synthesized
		String procedureSignature(ExprProc proc);

		@Procedural
		void functionDeclarations(ActorMachine actorMachine, PrintWriter writer);

		@Procedural
		void functionBody(ExprLet let, PrintWriter writer);

		@Procedural
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

	public String varInit(ExprLambda lambda, String name) {
		return "";
	}

	public String varInit(ExprProc procedure, String name) {
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

	public void functionBody(Expression expr, PrintWriter writer) {
		writer.println("{");
		String value = e().simpleExpression(expr);
		writer.println("return " + value + ";");
		writer.println("}");
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
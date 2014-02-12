package net.opendf.backend.c.att;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import javarag.CollectionBuilder;
import javarag.CollectionContribution;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.CollectionWrapper;
import net.opendf.backend.c.CType;
import net.opendf.backend.c.util.Joiner;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.util.ImmutableList;

public class Functions extends Module<Functions.Required> {
	private final Joiner pars = new Joiner("(", ", ", ")");
	interface Required {
		ActorMachine actorMachine(IRNode node);
		Collection<ExprLambda> lambdaExpressions(ActorMachine am);
		Collection<ExprProc> procedureExpressions(ActorMachine am);
		String lambdaSignature(ExprLambda lambda);
		String procedureSignature(ExprProc proc);
		String functionName(ExprLambda lambda);
		String functionName(ExprProc proc);
		String variableName(ParDeclValue par);
		String variableName(DeclVar decl);
		CType ctype(TypeExpr type);
		String simpleExpression(Expression e);
		void functionBody(Expression e, PrintWriter w);
		void functionBody(Statement s, PrintWriter w);
		String blockified(Statement s);
	}

	@Synthesized
	public String scopeVarInit(ExprLambda lambda, DeclVar varDecl) {
		return "";
	}

	@Synthesized
	public String scopeVarInit(ExprProc procedure, DeclVar varDecl) {
		return "";
	}

	@Synthesized
	public String lambdaSignature(ExprLambda lambda) {
		String name = get().functionName(lambda);
		List<String> parList = parList(lambda.getValueParameters());
		CType type = get().ctype(lambda.getReturnType());
		return new StringBuilder()
			.append(type.plainType())
			.append(" ")
			.append(name)
			.append(pars.join(parList))
			.toString();
	}

	@Synthesized
	public String procedureSignature(ExprProc proc) {
		String name = get().functionName(proc);
		List<String> parList = parList(proc.getValueParameters());
		return new StringBuilder()
			.append("void ")
			.append(name)
			.append(pars.join(parList))
			.toString();

	}

	@Synthesized
	public void functionDeclarations(ActorMachine actorMachine, PrintWriter writer) {
		Collection<ExprLambda> lambdas = get().lambdaExpressions(actorMachine);
		for (ExprLambda lambda : lambdas) {
			writer.print(get().lambdaSignature(lambda));
			writer.println(";");
		}

		Collection<ExprProc> procs = get().procedureExpressions(actorMachine);
		for (ExprProc proc : procs) {
			writer.print(get().procedureSignature(proc));
			writer.println(";");
		}
	}

	@Synthesized
	public void functionBody(ExprLet let, PrintWriter writer) {
		writer.println("{");
		for (DeclVar decl : let.getVarDecls()) {
			CType type = get().ctype(decl.getType());
			String name = get().variableName(decl);
			String value = get().simpleExpression(decl.getInitialValue());
			writer.print("const ");
			writer.print(type.variableType(name));
			writer.print(" = ");
			writer.print(value);
			writer.println(";");
		}
		String value = get().simpleExpression(let.getBody());
		writer.println("return "+value+";");
		writer.println("}");
	}

	@Synthesized
	public void functionDefinitions(ActorMachine actorMachine, PrintWriter writer) {
		Collection<ExprLambda> lambdas = get().lambdaExpressions(actorMachine);
		for (ExprLambda lambda : lambdas) {
			writer.println(get().lambdaSignature(lambda));
			get().functionBody(lambda.getBody(), writer);
		}

		Collection<ExprProc> procs = get().procedureExpressions(actorMachine);
		for (ExprProc proc : procs) {
			writer.println(get().procedureSignature(proc));
			writer.println(get().blockified(proc.getBody()));
		}
	}

	private List<String> parList(ImmutableList<ParDeclValue> pars) {
		List<String> parList = new ArrayList<>();
		for (ParDeclValue par : pars) {
			String name = get().variableName(par);
			CType type = get().ctype(par.getType());
			parList.add(type.variableType(name));
		}
		return parList;
	}

	@CollectionBuilder("lambdaExpressions")
	@Synthesized
	public Builder lambdaExpressions(ActorMachine actorMachine) {
		return new CollectionWrapper(new LinkedHashSet<>());
	}

	@CollectionContribution
	@Synthesized
	public void lambdaExpressions(ExprLambda lambda) {
		ActorMachine am = get().actorMachine(lambda);
		contribute(am, lambda);
	}

	@CollectionBuilder("procedureExpressions")
	@Synthesized
	public Builder procedureExpressions(ActorMachine actorMachine) {
		return new CollectionWrapper(new LinkedHashSet<>());
	}

	@CollectionContribution
	@Synthesized
	public void procedureExpressions(ExprProc proc) {
		ActorMachine am = get().actorMachine(proc);
		contribute(am, proc);
	}


}
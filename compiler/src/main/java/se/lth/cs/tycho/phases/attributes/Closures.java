package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public interface Closures {
	ModuleKey<Closures> key = (unit, attr) -> MultiJ.from(Closures.class)
			.bind("names").to(attr.getAttributeModule(Names.key, unit))
			.instance();

	@Binding(BindingKind.INJECTED)
	Names names();

	Set<VarDecl> freeVariables(Expression expr);

	@Binding(BindingKind.LAZY)
	default Map<ExprLambda, Set<VarDecl>> freeVariablesLambdaCache() {
		return new HashMap<>();
	}

	@Binding(BindingKind.LAZY)
	default Map<ExprProc, Set<VarDecl>> freeVariablesProcCache() {
		return new HashMap<>();
	}

	default Set<VarDecl> freeVariables(ExprVariable var) {
		return Collections.singleton(names().declaration(var.getVariable()));
	}

	default Set<VarDecl> freeVariables(ExprLambda expr) {
		return freeVariablesLambdaCache().computeIfAbsent(expr, lambda -> freeVariables(lambda.getBody()).stream()
				.filter(decl -> !lambda.getValueParameters().contains(decl))
				.collect(Collectors.toSet()));
	}

	default Set<VarDecl> freeVariables(ExprProc expr) {
		return freeVariablesProcCache().computeIfAbsent(expr, proc -> freeVariables(proc.getBody()).stream()
				.filter(decl -> !proc.getValueParameters().contains(decl))
				.collect(Collectors.toSet()));
	}

	default Set<VarDecl> freeVariables(ExprLet let) {
		return Stream.concat(Stream.of(let.getBody()), let.getVarDecls().stream().map(VarDecl::getValue))
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.distinct()
				.filter(decl -> !let.getVarDecls().contains(decl))
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(ExprBinaryOp expr) {
		return expr.getOperands().stream()
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(ExprLiteral lit) {
		return Collections.emptySet();
	}

	default Set<VarDecl> freeVariables(ExprUnaryOp unary) {
		return freeVariables(unary.getOperand());
	}

	default Set<VarDecl> freeVariables(ExprIndexer indexer) {
		return Stream.of(indexer.getStructure(), indexer.getIndex())
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(ExprApplication apply) {
		return Stream.concat(Stream.of(apply.getFunction()), apply.getArgs().stream())
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(ExprField field) {
		return freeVariables(field.getStructure());
	}

	default Set<VarDecl> freeVariables(ExprIf expr) {
		Set<VarDecl> result = new HashSet<>();
		result.addAll(freeVariables(expr.getCondition()));
		result.addAll(freeVariables(expr.getThenExpr()));
		result.addAll(freeVariables(expr.getElseExpr()));
		return result;
	}

	default Set<VarDecl> freeVariables(ExprComprehension comprehension) {
		Set<VarDecl> result = new HashSet<>();
		result.addAll(freeVariables(comprehension.getGenerator().getCollection()));
		comprehension.getFilters().forEach(filter -> result.addAll(freeVariables(filter)));
		result.addAll(freeVariables(comprehension.getCollection()));
		return result;
	}

	default Set<VarDecl> freeVariables(ExprList list) {
		return list.getElements().stream()
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(ExprInput input) {
		return Collections.emptySet();
	}

	Set<VarDecl> freeVariables(Statement stmt);

	default Set<VarDecl> freeVariables(StmtAssignment assign) {
		Set<VarDecl> result = new HashSet<>();
		result.addAll(freeVariables(assign.getLValue()));
		result.addAll(freeVariables(assign.getExpression()));
		return result;
	}

	default Set<VarDecl> freeVariables(StmtBlock block) {
		return block.getStatements().stream()
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.filter(decl -> !block.getVarDecls().contains(decl))
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(StmtCall call) {
		return Stream.concat(Stream.of(call.getProcedure()), call.getArgs().stream())
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(StmtConsume consume) {
		return Collections.emptySet();
	}

	default Set<VarDecl> freeVariables(StmtIf stmt) {
		Set<VarDecl> result = new HashSet<>();
		result.addAll(freeVariables(stmt.getCondition()));
		stmt.getThenBranch().forEach(s -> result.addAll(freeVariables(s)));
		stmt.getElseBranch().forEach(s -> result.addAll(freeVariables(s)));
		return result;
	}

	default Set<VarDecl> freeVariables(StmtRead read) {
		Set<VarDecl> result = new HashSet<>();
		read.getLValues().forEach(lvalue -> result.addAll(freeVariables(lvalue)));
		if (read.getRepeatExpression() != null) {
			result.addAll(freeVariables(read.getRepeatExpression()));
		}
		return result;
	}

	default Set<VarDecl> freeVariables(StmtWhile stmt) {
		return Stream.concat(
				freeVariables(stmt.getCondition()).stream(),
				stmt.getBody().stream()
						.map(this::freeVariables)
						.flatMap(Set::stream))
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(StmtWrite write) {
		Set<VarDecl> result = new HashSet<>();
		write.getValues().forEach(expr -> result.addAll(freeVariables(expr)));
		if (write.getRepeatExpression() != null) {
			result.addAll(freeVariables(write.getRepeatExpression()));
		}
		return result;
	}

	default Set<VarDecl> freeVariables(StmtForeach foreach) {
		Set<VarDecl> free = new HashSet<>();
		free.addAll(freeVariables(foreach.getGenerator()));
		foreach.getFilters().forEach(filter -> free.addAll(freeVariables(filter)));
		foreach.getBody().forEach(s -> free.addAll(freeVariables(s)));
		free.removeAll(foreach.getGenerator().getVarDecls());
		return free;
	}

	default Set<VarDecl> freeVariables(Generator generator) {
		Set<VarDecl> free = new HashSet<>();
		free.addAll(freeVariables(generator.getCollection()));
		free.removeAll(generator.getVarDecls());
		return free;
	}

	Set<VarDecl> freeVariables(LValue lvalue);

	default Set<VarDecl> freeVariables(LValueIndexer lvalue) {
		return Stream.concat(freeVariables(lvalue.getStructure()).stream(), freeVariables(lvalue.getIndex()).stream())
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(LValueField lvalue) {
		return freeVariables(lvalue.getStructure());
	}

	default Set<VarDecl> freeVariables(LValueVariable lvalue) {
		return Collections.singleton(names().declaration(lvalue.getVariable()));
	}

	default Set<PortDecl> freePortReferences(IRNode node) {
		FreePortReferenceCollector coll = MultiJ.from(FreePortReferenceCollector.class)
				.bind("names").to(names())
				.instance();
		coll.accept(node);
		return coll.collection();
	}

	@Module
	interface FreePortReferenceCollector extends Consumer<IRNode> {
		@Binding(BindingKind.INJECTED)
		Names names();

		@Binding
		default Set<PortDecl> collection() {
			return new HashSet<>();
		}

		@Override
		default void accept(IRNode node) {
			collectFromNode(node);
			node.forEachChild(this);
		}

		default void collectFromNode(IRNode node) {}
		default void collectFromNode(Port port) {
			collection().add(names().portDeclaration(port));
		}
	}
}

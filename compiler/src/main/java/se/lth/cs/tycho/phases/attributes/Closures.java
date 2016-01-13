package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.GeneratorFilter;
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
import java.util.List;
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

	default Set<VarDecl> freeVariables(ExprList list) {
		Set<VarDecl> free = new HashSet<>();
		free.addAll(freeVariablesOfGenerators(list.getGenerators()));
		Set<VarDecl> declared = declarationsInGenerators(list.getGenerators());
		list.getElements().stream()
				.map(this::freeVariables)
				.flatMap(Set::stream)
				.filter(decl -> !declared.contains(decl))
				.forEach(free::add);
		return free;
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
		result.addAll(freeVariables(stmt.getThenBranch()));
		if (stmt.getElseBranch() != null) {
			result.addAll(freeVariables(stmt.getElseBranch()));
		}
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
		return Stream.concat(freeVariables(stmt.getCondition()).stream(), freeVariables(stmt.getBody()).stream())
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

	default Set<VarDecl> freeVariablesOfGenerators(List<GeneratorFilter> generators) {
		Set<VarDecl> free = new HashSet<>();
		Set<VarDecl> declared = new HashSet<>();
		for (GeneratorFilter generator : generators) {
			freeVariables(generator.getCollectionExpr()).stream()
					.filter(decl -> !declared.contains(decl))
					.forEach(free::add);
			declared.addAll(generator.getVariables());
			generator.getFilters().stream()
					.map(this::freeVariables)
					.flatMap(Set::stream)
					.filter(decl -> !declared.contains(decl))
					.forEach(free::add);
		}
		return free;
	}

	default Set<VarDecl> declarationsInGenerators(List<GeneratorFilter> generators) {
		return generators.stream()
				.map(GeneratorFilter::getVariables)
				.flatMap(List::stream)
				.collect(Collectors.toSet());
	}

	default Set<VarDecl> freeVariables(StmtForeach foreach) {
		Set<VarDecl> free = new HashSet<>();
		free.addAll(freeVariablesOfGenerators(foreach.getGenerators()));
		Set<VarDecl> declared = declarationsInGenerators(foreach.getGenerators());
		freeVariables(foreach.getBody()).stream()
				.filter(decl -> !declared.contains(decl))
				.forEach(free::add);
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

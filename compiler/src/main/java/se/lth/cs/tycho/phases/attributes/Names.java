package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public interface Names {
	ModuleKey<Names> key = (unit, manager) -> MultiJ.from(Implementation.class)
			.bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
			.bind("globalNames").to(manager.getAttributeModule(GlobalNames.key, unit))
			.instance();


	VarDecl declaration(Variable var);

	PortDecl portDeclaration(Port port);

	GlobalEntityDecl entityDeclaration(EntityReference reference);

	@Module
	interface Implementation extends Names, PortNames, VariableNames, EntityNames, Util {
		@Binding
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();
		
		default VarDecl declaration(Variable var) {
			return VariableNames.super.declaration(var);
		}

		default PortDecl portDeclaration(Port port) {
			return PortNames.super.portDeclaration(port);
		}

	}

	interface Util {
		default <D extends Decl> Optional<D> findInStream(Stream<D> decls, String name) {
			return decls.filter(decl -> decl.getName().equals(name)).findAny();
		}

	}

	interface PortNames extends Names {

		TreeShadow tree();

		@Binding
		default Map<Port, PortDecl> portDeclarationMap() {
			return new ConcurrentHashMap<>();
		}

		default PortDecl portDeclaration(Port port) {
			return startPortLookup(port);
//			return portDeclarationMap().computeIfAbsent(port, this::startPortLookup);
		}

		default PortDecl startPortLookup(Port port) {
			return lookupPort(tree().parent(port), port);
		}

		PortDecl lookupPort(IRNode node, Port port);

		default PortDecl lookupPort(InputPattern input, Port port) {
			return lookupInputPort(input, port);
		}

		default PortDecl lookupPort(OutputExpression output, Port port) {
			return lookupOutputPort(tree().parent(output), port);
		}

		default PortDecl lookupPort(ExprInput input, Port port) {
			return lookupInputPort(input, port);
		}

		default PortDecl lookupPort(StmtRead read, Port port) {
			return lookupInputPort(read, port);
		}

		default PortDecl lookupPort(StmtWrite write, Port port) {
			return lookupOutputPort(write, port);
		}

		default PortDecl lookupPort(StmtConsume consume, Port port) {
			return lookupInputPort(consume, port);
		}

		default PortDecl lookupPort(PortCondition cond, Port port) {
			if (cond.isInputCondition()) {
				return lookupInputPort(cond, port);
			} else {
				return lookupOutputPort(cond, port);
			}
		}

		default PortDecl lookupInputPort(IRNode node, Port port) {
			return lookupInputPort(tree().parent(node), port);
		}

		default PortDecl lookupInputPort(Entity entity, Port port) {
			for (PortDecl decl : entity.getInputPorts()) {
				if (decl.getName().equals(port.getName())) {
					return decl;
				}
			}
			return null;
		}

		default PortDecl lookupOutputPort(IRNode node, Port port) {
			return lookupOutputPort(tree().parent(node), port);
		}

		default PortDecl lookupOutputPort(Entity entity, Port port) {
			for (PortDecl decl : entity.getOutputPorts()) {
				if (decl.getName().equals(port.getName())) {
					return decl;
				}
			}
			return null;
		}
	}

	interface EntityNames extends Names, Util {

		TreeShadow tree();

		GlobalNames globalNames();

		default GlobalEntityDecl entityDeclaration(EntityReference reference) {
			return lookupEntity(reference);
		}

		GlobalEntityDecl lookupEntity(EntityReference reference);

		default GlobalEntityDecl lookupEntity(EntityReferenceGlobal reference) {
			return globalNames().entityDecl(reference.getGlobalName(), false);
		}

		default GlobalEntityDecl lookupEntity(EntityReferenceLocal reference) {
			return localEntityLookup(reference, reference.getName());
		}

		default GlobalEntityDecl localEntityLookup(IRNode node, String name) {
			IRNode parent = tree().parent(node);
			return parent == null ? null : localEntityLookup(parent, name);
		}

		default GlobalEntityDecl localEntityLookup(NamespaceDecl namespaceDecl, String name) {
			return findInStream(namespaceDecl.getEntityDecls().stream(), name)
					.orElseGet(() -> globalNames().entityDecl(namespaceDecl.getQID().concat(QID.of(name)), true));
		}

	}

	interface VariableNames extends Names, Util {
		TreeShadow tree();

		GlobalNames globalNames();

		@Binding
		default Map<Variable, VarDecl> declarationMap() {
			return new ConcurrentHashMap<>();
		}

		default VarDecl declaration(Variable var) {
			return declarationMap().computeIfAbsent(var, v -> lookup(v, v.getName()));
		}

		default VarDecl lookup(IRNode context, String name) {
			IRNode node = tree().parent(context);
			while (node != null) {
				Optional<? extends VarDecl> d = localLookup(node, context, name);
				if (d.isPresent()) {
					return d.get();
				}
				context = node;
				node = tree().parent(node);
			}
			return null;
		}

		default Optional<? extends VarDecl> localLookup(IRNode node, IRNode context, String name) {
			return Optional.empty();
		}

		default Optional<? extends VarDecl> localLookup(ExprLet let, IRNode context, String name) {
			return findInStream(let.getVarDecls().stream(), name);
		}

		static <A, B extends A> Optional<A> upCast(Optional<B> opt) {
			return Optional.ofNullable(opt.orElse(null));
		}

		default Optional<? extends VarDecl> localLookup(ExprLambda lambda, IRNode context, String name) {
			return upCast(findInStream(Stream.concat(lambda.getClosure().stream(), lambda.getValueParameters().stream()), name));
		}

		default Optional<? extends VarDecl> localLookup(ExprProc proc, IRNode context, String name) {
			return upCast(findInStream(Stream.concat(proc.getClosure().stream(), proc.getValueParameters().stream()), name));
		}

		default Optional<? extends VarDecl> localLookup(StmtBlock block, IRNode context, String name) {
			return findInStream(block.getVarDecls().stream(), name);
		}

		default Optional<? extends VarDecl> localLookup(ExprComprehension comprehension, IRNode context, String name) {
			return upCast(findInStream(comprehension.getGenerator().getVarDecls().stream(), name));
		}

		default Optional<? extends VarDecl> localLookup(ExprComprehension comprehension, Generator context, String name) {
			return Optional.empty();
		}

		default Optional<? extends VarDecl> localLookup(Generator generator, IRNode context, String name) {
			return Optional.empty();
		}

		default Optional<? extends VarDecl> localLookup(StmtForeach foreach, IRNode context, String name) {
			return upCast(findInStream(foreach.getGenerator().getVarDecls().stream(), name));
		}

		default Optional<? extends VarDecl> localLookup(StmtForeach foreach, Generator context, String name) {
			return Optional.empty();
		}

		default Optional<? extends VarDecl> localLookup(Action action, IRNode context, String name) {
			Stream<? extends VarDecl> actionVars = action.getVarDecls().stream();
			Stream<? extends VarDecl> inputVars = action.getInputPatterns().stream()
					.flatMap(inputPattern -> inputPattern.getVariables().stream());

			return findInStream(Stream.concat(actionVars, inputVars), name);
		}

		default Optional<VarDecl> localLookup(CalActor actor, IRNode context, String name) {
			return findInStream(Stream.concat(actor.getVarDecls().stream(), actor.getValueParameters().stream()), name);
		}

		default Optional<VarDecl> localLookup(NlNetwork network, IRNode context, String name) {
			return findInStream(Stream.concat(network.getVarDecls().stream(), network.getValueParameters().stream()), name);
		}

		default Optional<VarDecl> localLookup(ActorMachine actorMachine, IRNode context, String name) {
			return findInStream(Stream.concat(actorMachine.getScopes().stream().flatMap(s -> s.getDeclarations().stream()), actorMachine.getValueParameters().stream()), name);
		}

		default Optional<VarDecl> localLookup(NamespaceDecl ns, IRNode context, String name) {
			Optional<GlobalVarDecl> result = this.findInStream(ns.getVarDecls().stream(), name);
			if (result.isPresent()) {
				return Optional.of(result.get());
			}
			VarDecl inNamespace = globalNames().varDecl(ns.getQID().concat(QID.of(name)), true);
			if (inNamespace != null) {
				return Optional.of(inNamespace);
			}
			for (Import imp : ns.getImports()) {
				Optional<VarDecl> decl = varImportLookup(imp, name);
				if (decl.isPresent()) return decl;
			}
			return Optional.empty();
		}

		Optional<VarDecl> varImportLookup(Import imp, String name);
		default Optional<VarDecl> varImportLookup(SingleImport singleImport, String name) {
			if (singleImport.getKind() == Import.Kind.VAR && singleImport.getLocalName().equals(name)) {
				return Optional.of(globalNames().varDecl(singleImport.getGlobalName(), false));
			} else {
				return Optional.empty();
			}
		}

		default Optional<VarDecl> varImportLookup(GroupImport groupImport, String name) {
			if (groupImport.getKind() == Import.Kind.VAR) {
				QID globalName = groupImport.getGlobalName().concat(QID.of(name));
				return Optional.ofNullable(globalNames().varDecl(globalName, false));
			} else {
				return Optional.empty();
			}
		}

		default Optional<VarDecl> localLookup(CompilationTask task, IRNode context, String name) {
			return Optional.ofNullable(globalNames().varDecl(QID.of("prelude", name), false));
		}

	}
}

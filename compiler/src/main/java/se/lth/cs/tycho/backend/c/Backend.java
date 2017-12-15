package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.attribute.ActorMachineScopes;
import se.lth.cs.tycho.attribute.Closures;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.FreeVariables;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.attribute.ScopeDependencies;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.UniqueNumbers;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.phase.TreeShadow;
import se.lth.cs.tycho.backend.c.util.Box;

import static org.multij.BindingKind.INJECTED;
import static org.multij.BindingKind.LAZY;
import static org.multij.BindingKind.MODULE;

@Module
public interface Backend {
	// Attributes
	@Binding(INJECTED) CompilationTask task();
	@Binding(INJECTED) Context context();

	@Binding(LAZY) default Box<Instance> instance() { return Box.empty(); }
	@Binding(LAZY) default Emitter emitter() { return new Emitter(); };
	@Binding(LAZY) default Types types() {
		return task().getModule(Types.key);
	}
	@Binding(LAZY) default ConstantEvaluator constants() {
		return task().getModule(ConstantEvaluator.key);
	}
	@Binding(LAZY) default VariableDeclarations varDecls() {
		return task().getModule(VariableDeclarations.key);
	}
	@Binding(LAZY) default GlobalNames globalNames() {
		return task().getModule(GlobalNames.key);
	}
	@Binding(LAZY) default UniqueNumbers uniqueNumbers() { return context().getUniqueNumbers(); }
	@Binding(LAZY) default TreeShadow tree() {
		return task().getModule(TreeShadow.key);
	}
	@Binding(LAZY) default ActorMachineScopes scopes() {
		return task().getModule(ActorMachineScopes.key);
	}
	@Binding(LAZY) default Closures closures() {
		return task().getModule(Closures.key);
	}
	@Binding(LAZY) default FreeVariables freeVariables() {
		return task().getModule(FreeVariables.key);
	}
	@Binding(LAZY) default ScopeDependencies scopeDependencies() {
		return task().getModule(ScopeDependencies.key);
	}

	// Code generator
	@Binding(MODULE) Lists lists();
	@Binding(MODULE) Variables variables();
	@Binding(MODULE) Structure structure();
	@Binding(MODULE) Code code();
	@Binding(MODULE) Controllers controllers();
	@Binding(MODULE) Main main();
	@Binding(MODULE) MainNetwork mainNetwork();
	@Binding(MODULE) Global global();
	@Binding(MODULE) DefaultValues defaultValues();
	@Binding(MODULE) Callables callables();
	@Binding(MODULE) AlternativeChannels channels();
}

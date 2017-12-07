package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.UniqueNumbers;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.phases.TreeShadow;
import se.lth.cs.tycho.phases.TreeShadowNew;
import se.lth.cs.tycho.phases.attributes.*;
import se.lth.cs.tycho.phases.cbackend.util.Box;

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
	@Binding(LAZY) default Types types() { return context().getAttributeManager().getAttributeModule(Types.key, task()); }
	@Binding(LAZY) default ConstantEvaluator constants() { return context().getAttributeManager().getAttributeModule(ConstantEvaluator.key, task()); }
	@Binding(LAZY) default VariableDeclarations varDecls() { return context().getAttributeManager().getAttributeModule(VariableDeclarations.key, task()); }
	@Binding(LAZY) default GlobalNames globalNames() { return context().getAttributeManager().getAttributeModule(GlobalNames.key, task()); }
	@Binding(LAZY) default UniqueNumbers uniqueNumbers() { return context().getUniqueNumbers(); }
	@Binding(LAZY) default TreeShadowNew tree() { return context().getAttributeManager().getAttributeModule(TreeShadow.key, task()); }
	@Binding(LAZY) default ActorMachineScopes scopes() { return context().getAttributeManager().getAttributeModule(ActorMachineScopes.key, task()); }
	@Binding(LAZY) default Closures closures() { return context().getAttributeManager().getAttributeModule(Closures.key, task()); }
	@Binding(LAZY) default FreeVariables freeVariables() { return context().getAttributeManager().getAttributeModule(FreeVariables.key, task()); }
	@Binding(LAZY) default ScopeDependencies scopeDependencies() { return context().getAttributeManager().getAttributeModule(ScopeDependencies.key, task()); }

	// Code generator
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

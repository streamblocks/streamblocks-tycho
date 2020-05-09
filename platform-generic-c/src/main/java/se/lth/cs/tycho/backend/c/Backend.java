package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ActorMachineScopes;
import se.lth.cs.tycho.attribute.Closures;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.FreeVariables;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.attribute.ScopeDependencies;
import se.lth.cs.tycho.attribute.TypeScopes;
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
	@Binding(LAZY) default TypeScopes typeScopes() {
		return task().getModule(TypeScopes.key);
	}
	// @Binding(LAZY) default ModuleMembers moduleMembers() {
		// return task().getModule(ModuleMembers.key);
	// }

	// Code generator
	@Binding(LAZY) default Lists lists() {
		return MultiJ.from(Lists.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Variables variables() {
		return MultiJ.from(Variables.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Structure structure() {
		return MultiJ.from(Structure.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Code code() {
		return MultiJ.from(Code.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Controllers controllers() {
		return MultiJ.from(Controllers.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Main main() {
		return MultiJ.from(Main.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default MainNetwork mainNetwork() {
		return MultiJ.from(MainNetwork.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Global global() {
		return MultiJ.from(Global.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default DefaultValues defaultValues() {
		return MultiJ.instance(DefaultValues.class);
	}
	@Binding(LAZY) default Callables callables() {
		return MultiJ.from(Callables.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default AlternativeChannels channels() {
		return MultiJ.from(AlternativeChannels.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default AlgebraicTypes algebraicTypes() {
		return MultiJ.from(AlgebraicTypes.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default PatternMatching patternMatching() {
		return MultiJ.from(PatternMatching.class).bind("backend").to(this).instance();
	}
	@Binding(LAZY) default Alias alias() {
		return MultiJ.from(Alias.class).bind("backend").to(this).instance();
	}

	// Utils
	@Binding(LAZY) default MemoryStack memoryStack() {
		return MultiJ.from(MemoryStack.class).bind("backend").to(this).instance();
	}
}

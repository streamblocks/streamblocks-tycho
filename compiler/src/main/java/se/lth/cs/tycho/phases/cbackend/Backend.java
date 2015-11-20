package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.UniqueNumbers;
import se.lth.cs.tycho.phases.TreeShadow;
import se.lth.cs.tycho.phases.attributes.ActorMachineScopes;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.phases.attributes.Types;

import static org.multij.BindingKind.INJECTED;
import static org.multij.BindingKind.MODULE;

@Module
public interface Backend {
	// Attributes
	@Binding(INJECTED) Types types();
	@Binding(INJECTED) Names names();
	@Binding(INJECTED) UniqueNumbers uniqueNumbers();
	@Binding(INJECTED) TreeShadow tree();
	@Binding(INJECTED) ActorMachineScopes scopes();

	// Code generator
	@Binding(MODULE) Variables variables();
	@Binding(MODULE) Emitter emitter();
	@Binding(MODULE) Structure structure();
	@Binding(MODULE) Code code();
	@Binding(MODULE) Controllers controllers();
	@Binding(MODULE)
	MainFunction main();
}

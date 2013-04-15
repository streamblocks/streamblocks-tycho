package net.opendf.ir.am;

import java.util.List;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;

/**
 * This class contains a description of an actor machine. The central structure
 * inside an actor machine is its controller.
 * 
 * The controller is a list that contains for each index (the <it>controller
 * state</it>) a (possibly empty) list of {@link Instruction instructions} that
 * may be executed in that state. In addition to the code required to execute
 * the instructions, they also contain one or more successor states that the
 * controller transitions to after execution.
 * 
 * The initial controller state is assumed to be 0, and the controller must at
 * least contain a single state.
 * 
 * The actual code that is executed is contained with the {@link ICall call} and
 * {@link ITest test} instructions.
 * 
 * Along with the controller, an actor machine contains a list of
 * {@link DeclVar declarations}. Each of these declarations represents a set of
 * temporary variable declarations that are referred to by the
 * {@link PredicateCondition predicate conditions} and the {@link Transition
 * transition code}. These bindings are valid until a {@link ICall instruction}
 * clears them.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ActorMachine extends AbstractIRNode implements PortContainer {

	public List<List<Instruction>> getController() {
		return controller;
	}

	public List<Instruction> getInstructions(int n) {
		return controller.get(n);
	}

	public List<DeclVar> getVarDecls() {
		return varDecls;
	}

	public List<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public List<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public ActorMachine(List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<DeclVar> varDecls,
			List<List<Instruction>> controller) {
		this.varDecls = varDecls;
		this.controller = controller;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
	}

	private List<DeclVar> varDecls;
	private List<List<Instruction>> controller;
	private List<PortDecl> inputPorts;
	private List<PortDecl> outputPorts;
}

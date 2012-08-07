
package net.opendf.ir.am;

import java.util.List;

import net.opendf.ir.common.CompositePortDecl;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDecl;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.PortContainer;


/**
 * This class contains a description of an actor machine. The central structure inside an actor machine is its controller.
 * 
 * The controller is an array that contains for each index (the <it>controller state</it>) a (possibly empty) list of
 * {@link Instruction instructions} that may be executed in that state. In addition to the code required to execute the 
 * instructions, they also contain one or more successor states that the controller transitions to after execution.
 * 
 * The initial controller state is assumed to be 0, and the controller array must at least contain a single element.
 * 
 * The actual code that is executed is contained with the {@link ICall call} and {@link ITest test} instructions.
 * 
 * Along with the controller, an actor machine contains an array of lists of {@link Decl declarations}. Each
 * of these declaration lists is called a <it>scope</it>, and it represents a set of temporary variable declarations that are
 * referred to by the {@link PredicateCondition predicate conditions} and the {@link Transition transition code}. These bindings
 * are valid until a {@link call instruction} clears them.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class ActorMachine implements PortContainer {
	
	public List<List<Instruction>>  getController() { return controller; }
	
	public List<Instruction>  getInstructions(int n) { return controller.get(n); }
	
	public List<Scope>  getScopes() { return scopes; }
	
	public CompositePortDecl getInputPorts() { return inputPorts; }
	
	public CompositePortDecl getOutputPorts() { return outputPorts; }

	//
	//  Ctor
	//
	
	public ActorMachine(CompositePortDecl inputPorts, CompositePortDecl outputPorts,
            List<Scope> scopes, List<List<Instruction>> controller
        )
    {
		this.scopes = scopes; 
        this.controller = controller;
        this.inputPorts = inputPorts;
        this.outputPorts = outputPorts;
    }
	
	private List<Scope>		  scopes;
	private List<List<Instruction>> controller;
	private CompositePortDecl inputPorts;
	private CompositePortDecl outputPorts;
}

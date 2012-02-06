
package net.opendf.ir.am;

import java.util.List;

import net.opendf.ir.common.CompositePortDecl;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDecl;


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

public class ActorMachine extends DeclEntity {
	
	public List<Instruction> []  getController() { return controller; }
	
	public List<Instruction>  getInstructions(int n) { return controller[n]; }
	
	public Scope []  getScopes() { return scopes; }
	
	//
	//  Ctor
	//
	
	public ActorMachine(String name, NamespaceDecl namespace,
			ParDecl [] parameters, Decl [] decls,
            CompositePortDecl inputPorts, CompositePortDecl outputPorts,
            Scope [] scopes, List<Instruction> [] controller
        )
    {
		super(name, namespace, parameters, decls);
		this.scopes = scopes;
        this.controller = controller;
    }
	
	private Scope []		  scopes;
	
	private List<Instruction> []  controller;
}

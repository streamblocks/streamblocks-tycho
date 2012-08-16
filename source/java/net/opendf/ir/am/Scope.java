package net.opendf.ir.am;

import java.util.Collections;
import java.util.List;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Decl;

/**
 * A scope in an actor machine is a collection of {@link Decl declarations} of temporary variables. 
 * To 'evaluate' a scope is to assign concrete values to each variable declared in it, by evaluating the initialization
 * expression in its declaration.
 * <p>A scope may 'require' other scopes: in that case, those need to be evaluated before the scope requiring them can be, which is the
 * case if the expressions in a scope rely on variables in another scope.
 * <p>All scopes see the parameters and declarations of the actor machine they are contained in, unless those are shadowed 
 * by a declaration inside the scope or one of its required scopes.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Scope extends AbstractIRNode {

	public enum ScopeKind {Persistent, Transient};
	
	public ScopeKind getKind() { return kind; }
	
	public List<Scope> getRequiredScopes() {
		return requiredScopes;
	}
	
	public List<Decl> getDeclarations() {
		return declarations;
	}

	//
	//  Ctor
	//
	
	public Scope(ScopeKind kind, List<Decl> declarations) {
		this (Collections.<Scope>emptyList(), kind, declarations);
	}
	
	public Scope(List<Scope> requiredScopes, ScopeKind kind, List<Decl> declarations) {
		this.requiredScopes = requiredScopes;
		this.kind = kind;
		this.declarations = declarations;
	}
	
	
	private List<Scope>  	requiredScopes;
	private ScopeKind		kind;
	private List<Decl>		declarations;

}

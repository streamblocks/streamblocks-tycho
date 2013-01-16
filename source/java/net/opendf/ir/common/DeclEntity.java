package net.opendf.ir.common;

import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

/**
 * PortContainer is the common base class of things that contain input and output ports, viz. {@link Network}s and {@link Node}s. 
 * Each collection of ports is represented by a top-level {@link CompositePortDecl}. 
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

abstract public class DeclEntity extends Decl implements PortContainer {

	
	public ParDeclType []  getTypeParameters() {
		return typePars;
	}
	
	public ParDeclValue []  getValueParameters() {
		return valuePars;
	}
	
	public DeclType []  getTypeDecls() {
		return typeDecls;
	}
	
	public DeclVar []  getVarDecls() {
		return varDecls;
	}
	
	// Decl

	@Override
	public DeclKind  getKind() { return DeclKind.entity; };

	@Override
	public <R,P> R accept(DeclVisitor<R,P> v, P p) {
		return v.visitDeclEntity(this, p);
	}
	
	
	// PortContainer

	public CompositePortDecl getInputPorts() {
		return inputPorts;
	}
	
	public CompositePortDecl getOutputPorts() {
		return outputPorts;
	}
	
	
	//
	// Ctor
	//

	public DeclEntity(String name, NamespaceDecl ns, ParDeclType [] typePars, ParDeclValue [] valuePars, DeclType [] typeDecls, DeclVar [] varDecls) {
		this (name, ns, typePars, valuePars, typeDecls, varDecls, new CompositePortDecl(null, null), new CompositePortDecl(null, null));
	}

	public DeclEntity(String name, NamespaceDecl ns, ParDeclType [] typePars, ParDeclValue [] valuePars, DeclType [] typeDecls, DeclVar [] varDecls, CompositePortDecl inputPorts, CompositePortDecl outputPorts) {
		super (name, ns);
		this.typePars = typePars;
		this.valuePars = valuePars;
		this.typeDecls = typeDecls;
		this.varDecls = varDecls;

		this.inputPorts = inputPorts != null ? inputPorts : new CompositePortDecl();
		this.inputPorts.setContainer(this);

		this.outputPorts = outputPorts != null ? outputPorts : new CompositePortDecl();
		this.outputPorts.setContainer(this);
	}
	

	private ParDeclType [] 	typePars;
	private ParDeclValue [] valuePars;
	private DeclType [] 	typeDecls;
	private DeclVar []		varDecls;
	
	private CompositePortDecl  inputPorts;
	private CompositePortDecl  outputPorts;

}

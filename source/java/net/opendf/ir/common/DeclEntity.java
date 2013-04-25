package net.opendf.ir.common;

import java.util.Arrays;
import java.util.List;

import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

/**
 * PortContainer is the common base class of things that contain input and output ports, viz. {@link Network}s and {@link Node}s. 
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

	public List<PortDecl> getInputPorts() {
		return inputPorts;
	}
	
	public List<PortDecl> getOutputPorts() {
		return outputPorts;
	}
	
	
	//
	// Ctor
	//

	public DeclEntity(String name, NamespaceDecl ns, ParDeclType [] typePars, ParDeclValue [] valuePars, DeclType [] typeDecls, DeclVar [] varDecls) {
		this (name, ns, typePars, valuePars, typeDecls, varDecls, new PortDecl[0], new PortDecl[0]);
	}

	public DeclEntity(String name, NamespaceDecl ns, ParDeclType [] typePars, ParDeclValue [] valuePars, DeclType [] typeDecls, DeclVar [] varDecls, PortDecl [] inputPorts, PortDecl [] outputPorts) {
		super (name, ns);
		this.typePars = typePars;
		this.valuePars = valuePars;
		this.typeDecls = typeDecls;
		this.varDecls = varDecls;

		this.inputPorts = Arrays.asList(inputPorts);
		this.outputPorts = Arrays.asList(outputPorts);
	}
	

	private ParDeclType [] 	typePars;
	private ParDeclValue [] valuePars;
	private DeclType [] 	typeDecls;
	private DeclVar []		varDecls;
	
	private List<PortDecl>  inputPorts;
	private List<PortDecl>  outputPorts;

}

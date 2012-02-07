package net.opendf.ir.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Decl.DeclKind;
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

	
	public ParDecl []  getParameters() {
		return pars;
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
	public void  accept(DeclVisitor v) {
		v.visitDeclEntity(this);
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

	public DeclEntity(String name, NamespaceDecl ns, ParDecl [] pars, DeclType [] typeDecls, DeclVar [] varDecls) {
		this (name, ns, pars, typeDecls, varDecls, new CompositePortDecl(null, null), new CompositePortDecl(null, null));
	}

	public DeclEntity(String name, NamespaceDecl ns, ParDecl [] pars, DeclType [] typeDecls, DeclVar [] varDecls, CompositePortDecl inputPorts, CompositePortDecl outputPorts) {
		super (name, ns);
		this.pars = pars;
		this.typeDecls = typeDecls;
		this.varDecls = varDecls;

		this.inputPorts = inputPorts;
		this.inputPorts.setContainer(this);

		this.outputPorts = outputPorts;
		this.outputPorts.setContainer(this);
	}
	

	private ParDecl [] 		pars;
	private DeclType [] 	typeDecls;
	private DeclVar []		varDecls;
	
	private CompositePortDecl  inputPorts;
	private CompositePortDecl  outputPorts;

}

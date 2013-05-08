package net.opendf.ir.common;

import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;

/**
 * PortContainer is the common base class of things that contain input and
 * output ports, viz. {@link Network}s and {@link Node}s. Each collection of
 * ports is represented by a top-level {@link CompositePortDecl}.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

abstract public class DeclEntity extends Decl implements PortContainer {

	public ImmutableList<ParDeclType> getTypeParameters() {
		return typePars;
	}

	public ImmutableList<ParDeclValue> getValueParameters() {
		return valuePars;
	}

	public ImmutableList<DeclType> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<DeclVar> getVarDecls() {
		return varDecls;
	}

	// Decl

	@Override
	public DeclKind getKind() {
		return DeclKind.entity;
	};

	@Override
	public <R, P> R accept(DeclVisitor<R, P> v, P p) {
		return v.visitDeclEntity(this, p);
	}

	// PortContainer

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	//
	// Ctor
	//

	public DeclEntity(DeclEntity original, String name, NamespaceDecl ns, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls) {
		this(original, name, ns, typePars, valuePars, typeDecls, varDecls, null, null);
	}

	public DeclEntity(DeclEntity original, String name, NamespaceDecl ns, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts) {
		super(original, name, ns);
		this.typePars = ImmutableList.copyOf(typePars);
		this.valuePars = ImmutableList.copyOf(valuePars);
		this.typeDecls = ImmutableList.copyOf(typeDecls);
		this.varDecls = ImmutableList.copyOf(varDecls);
		this.inputPorts = ImmutableList.copyOf(inputPorts);
		this.outputPorts = ImmutableList.copyOf(outputPorts);
	}

	private ImmutableList<ParDeclType> typePars;
	private ImmutableList<ParDeclValue> valuePars;
	private ImmutableList<DeclType> typeDecls;
	private ImmutableList<DeclVar> varDecls;

	private ImmutableList<PortDecl> inputPorts;
	private ImmutableList<PortDecl> outputPorts;

}

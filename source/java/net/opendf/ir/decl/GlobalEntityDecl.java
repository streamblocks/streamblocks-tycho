package net.opendf.ir.decl;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.entity.PortContainer;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;

/**
 * PortContainer is the common base class of things that contain input and
 * output ports, viz. {@link Network}s and {@link Node}s.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

abstract public class GlobalEntityDecl extends AbstractIRNode implements GlobalDecl, PortContainer {
	@Override
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalEntityDecl(this, param);
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public Visibility getVisibility() {
		return Visibility.PUBLIC;
	}

	public ImmutableList<ParDeclType> getTypeParameters() {
		return typePars;
	}

	public ImmutableList<ParDeclValue> getValueParameters() {
		return valuePars;
	}

	public ImmutableList<LocalTypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
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

	public GlobalEntityDecl(GlobalEntityDecl original, String name, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls,
			ImmutableList<LocalVarDecl> varDecls) {
		this(original, name, typePars, valuePars, typeDecls, varDecls, null, null);
	}

	public GlobalEntityDecl(GlobalEntityDecl original, String name, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls,
			ImmutableList<LocalVarDecl> varDecls, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts) {
		super(original);
		this.name = name;
		this.typePars = ImmutableList.copyOf(typePars);
		this.valuePars = ImmutableList.copyOf(valuePars);
		this.typeDecls = ImmutableList.copyOf(typeDecls);
		this.varDecls = ImmutableList.copyOf(varDecls);
		this.inputPorts = ImmutableList.copyOf(inputPorts);
		this.outputPorts = ImmutableList.copyOf(outputPorts);
	}

	private String name;
	
	private ImmutableList<ParDeclType> typePars;
	private ImmutableList<ParDeclValue> valuePars;
	private ImmutableList<LocalTypeDecl> typeDecls;
	private ImmutableList<LocalVarDecl> varDecls;

	private ImmutableList<PortDecl> inputPorts;
	private ImmutableList<PortDecl> outputPorts;
}

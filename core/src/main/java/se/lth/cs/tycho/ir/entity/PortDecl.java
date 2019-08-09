package se.lth.cs.tycho.ir.entity;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;

/**
 * Port declaration.
 */
public class PortDecl extends AbstractIRNode {
	private String name;
	private TypeExpr type;

	/**
	 * Constructs a port with a name.
	 * 
	 * @param name
	 *            the port name
	 */
	public PortDecl(String name) {
		this(null, name, null);
	}

	/**
	 * Constructs a port with a name and a type.
	 * 
	 * @param name
	 *            the port name
	 * @param type
	 *            the type of the tokens
	 */
	public PortDecl(String name, TypeExpr type) {
		this(null, name, type);
	}
	
	private PortDecl(PortDecl original, String name, TypeExpr type) {
		super(original);
		this.name = name;
		this.type = type;
	}
	
	public PortDecl copy(String name, TypeExpr type) {
		if (Objects.equals(this.name, name) && this.type == type) {
			return this;
		}
		return new PortDecl(this, name, type);
	}

	/**
	 * Returns the name of the port.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a safe name of the port
	 *
	 * @return a safe name
	 */
	public String getSafeName() {
		if(name.equalsIgnoreCase("IN")){
			return name + "_r";
		}else if(name.equalsIgnoreCase("OUT")){
			return name + "_r";
		}else if(name.equalsIgnoreCase("BYTE")){
			return name + "_r";
		}else{
			return name;
		}

	}

	public PortDecl withName(String name) {
		return copy(name, type);
	}

	/**
	 * Returns the type of the tokens on port.
	 * 
	 * @return the type of the tokens
	 */
	public TypeExpr getType() {
		return type;
	}

	public PortDecl withType(TypeExpr type) {
		return copy(name, type);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (type != null) action.accept(type);
	}

	@Override
	public PortDecl transformChildren(Transformation transformation) {
		return copy(name, type == null ? null : (TypeExpr) transformation.apply(type));
	}

	@Override
	public PortDecl clone() {
		return (PortDecl) super.clone();
	}

	@Override
	public PortDecl deepClone() {
		return (PortDecl) super.deepClone();
	}
}

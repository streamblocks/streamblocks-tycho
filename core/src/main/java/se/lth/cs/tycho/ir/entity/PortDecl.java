package se.lth.cs.tycho.ir.entity;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.TypeExpr;

/**
 * Port declaration.
 */
public class PortDecl extends AbstractIRNode {
	private String name;
	private TypeExpr type;

	// This array initializer expression falls within the square brackets of the Port declaration and should specify
	// the size of the array. As such we expect the value of this expression to be computable at compile time.
	//
	// If this value is null then the port is not an array of ports only a single port is represented.
	private Expression arrayInitExpr;

	/**
	 * Constructs a port with a name.
	 * 
	 * @param name
	 *            the port name
	 */
	public PortDecl(String name) {
		this(null, name, null, null);
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
		this(null, name, type, null);
	}

	/**
	 * Constructs a port with a name and a type.
	 *
	 * @param name
	 *            the port name
	 * @param type
	 *            the type of the token
	 * @param arrayInitExpr
	 * 	 		  expression defining size of the port array
	 */
	public PortDecl(String name, TypeExpr type, Expression arrayInitExpr) {
		this(null, name, type, arrayInitExpr);
	}

	
	private PortDecl(PortDecl original, String name, TypeExpr type, Expression arrayInitExpr) {
		super(original);
		this.name = name;
		this.type = type;
		this.arrayInitExpr = arrayInitExpr;
	}
	
	public PortDecl copy(String name, TypeExpr type, Expression arrayInitExpr) {
		if (Objects.equals(this.name, name) && this.type == type && Objects.equals(this.arrayInitExpr, arrayInitExpr)) {
			return this;
		}
		return new PortDecl(this, name, type, arrayInitExpr);
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

	/**
	 * Returns the array initialisation expression of the port.
	 *
	 * @return the array initialisation expression of the port.
	 */

	public Expression getArrayInitExpr() {
		return arrayInitExpr;
	}

	public PortDecl withName(String name) {
		return copy(name, type, arrayInitExpr);
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
		return copy(name, type, arrayInitExpr);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (type != null) action.accept(type);
		if (arrayInitExpr != null) action.accept(arrayInitExpr);
	}

	@Override
	public PortDecl transformChildren(Transformation transformation) {
		return copy(name,
				type == null ? null : (TypeExpr) transformation.apply(type),
				arrayInitExpr == null ? null : (Expression) transformation.apply(arrayInitExpr));
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

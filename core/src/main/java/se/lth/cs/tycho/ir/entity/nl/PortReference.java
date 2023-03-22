package se.lth.cs.tycho.ir.entity.nl;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * @author Per Andersson
 */
public class PortReference extends AbstractIRNode {

	public PortReference(String entityName, ImmutableList<Expression> entityIndex, String portName, ImmutableList<Expression> arrayIndexExpr) {
		this(null, entityName, entityIndex, portName, arrayIndexExpr);
	}

	private PortReference(PortReference original, String entityName, ImmutableList<Expression> entityIndex,
			String portName, ImmutableList<Expression> arrayIndexExpr) {
		super(original);
		this.entityName = entityName;
		this.entityIndex = ImmutableList.from(entityIndex);
		this.portName = portName;
		this.arrayIndexExpr = ImmutableList.from(arrayIndexExpr);
	}

	public PortReference copy(String entityName, ImmutableList<Expression> entityIndex, String portName, ImmutableList<Expression> arrayIndexExpr) {
		if (Objects.equals(this.entityName, entityName) && Lists.equals(this.entityIndex, entityIndex)
				&& Objects.equals(this.portName, portName) && Objects.equals(this.arrayIndexExpr, arrayIndexExpr)) {
			return this;
		}
		return new PortReference(this, entityName, entityIndex, portName, arrayIndexExpr);
	}

	public String getEntityName() {
		return entityName;
	}

	public ImmutableList<Expression> getEntityIndex() {
		return entityIndex;
	}

	public ImmutableList<Expression> getArrayIndexExpr() { return arrayIndexExpr; }

	public String getPortName() {
		return portName;
	}

	public PortReference withEntityName(String entityName) {
		if (Objects.equals(this.entityName, entityName)) {
			return this;
		} else {
			return new PortReference(this, entityName, entityIndex, portName, arrayIndexExpr);
		}
	}

	public PortReference withPortName(String portName) {
		if (Objects.equals(this.portName, portName)) {
			return this;
		} else {
			return new PortReference(this, entityName, entityIndex, portName, arrayIndexExpr);
		}
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		if (entityName != null) {
			result.append(entityName);
			if (entityIndex != null) {
				for (Expression e : entityIndex) {
					result.append('[');
					result.append(e.toString());
					result.append(']');
				}
			}
			result.append('.');
		}
		result.append(portName);
		return result.toString();
	}

	private String entityName, portName;
	private ImmutableList<Expression> entityIndex;

	/**
	 * @author Gareth Callanan
	 *
	 * This expression falls within the square brackets of the PortReference and is the index
	 * of the element in the array of ports to select. I expect the value of this expression to be computable at
	 * compile time. Size will either be 1 or 0
	 */
	private ImmutableList<Expression> arrayIndexExpr;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		entityIndex.forEach(action);
		arrayIndexExpr.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public PortReference transformChildren(Transformation transformation) {
		return copy(entityName, (ImmutableList) entityIndex.map(transformation), portName, (ImmutableList) arrayIndexExpr.map(transformation));
	}
}

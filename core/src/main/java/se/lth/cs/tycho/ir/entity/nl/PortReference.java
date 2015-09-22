package se.lth.cs.tycho.ir.entity.nl;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class PortReference extends AbstractIRNode {

	public PortReference(String entityName, ImmutableList<Expression> entityIndex, String portName) {
		this(null, entityName, entityIndex, portName);
	}

	private PortReference(PortReference original, String entityName, ImmutableList<Expression> entityIndex,
			String portName) {
		super(original);
		this.entityName = entityName;
		this.entityIndex = ImmutableList.copyOf(entityIndex);
		this.portName = portName;
	}

	public PortReference copy(String entityName, ImmutableList<Expression> entityIndex, String portName) {
		if (Objects.equals(this.entityName, entityName) && Lists.equals(this.entityIndex, entityIndex)
				&& Objects.equals(this.portName, portName)) {
			return this;
		}
		return new PortReference(this, entityName, entityIndex, portName);
	}

	public String getEntityName() {
		return entityName;
	}

	public ImmutableList<Expression> getEntityIndex() {
		return entityIndex;
	}

	public String getPortName() {
		return portName;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		entityIndex.forEach(action);
	}

	@Override
	public PortReference transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(entityName, (ImmutableList) entityIndex.map(transformation), portName);
	}
}

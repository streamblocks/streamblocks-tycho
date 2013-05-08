package net.opendf.ir.net.ast;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Expression;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

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
}

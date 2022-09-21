package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.List;
import java.util.stream.Collectors;

public class ValueList extends Value {

	private final List<Value> elements;

	public ValueList(List<Value> elements) {
		this.elements = elements;  this.type = null;
	}

	public List<Value> elements() {
		return elements;
	}

	@Override
	public String toString() {
		return elements.stream().map(Value::toString).collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public void setType(TypeExpr type){
		this.type = type;
	}
}

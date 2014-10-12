package se.lth.cs.tycho.classifier.util;

import java.util.NoSuchElementException;
import java.util.Objects;

import se.lth.cs.tycho.instance.am.ICall;

public class DecisionPathKnowledge {
	private final ImmutableBitSet trueConds;
	private final ImmutableBitSet falseConds;
	private final ICall destination;
	private ImmutableBitSet conditions;

	public DecisionPathKnowledge(ICall destination) {
		this(destination, ImmutableBitSet.empty(), ImmutableBitSet.empty());
	}
	
	private DecisionPathKnowledge(ICall destination, ImmutableBitSet trueConds, ImmutableBitSet falseConds) {
		this.destination = destination;
		this.trueConds = trueConds;
		this.falseConds = falseConds;
	}

	public DecisionPathKnowledge prepend(int cond, boolean result) {
		if (hasKnowledge(cond)) {
			return this;
		} else {
			return new DecisionPathKnowledge(destination, trueConds.set(cond, result), falseConds.set(cond, !result));
		}
	}
	
	public ImmutableBitSet getTrueConditions() {
		return trueConds;
	}
	
	public ImmutableBitSet getFalseConditions() {
		return falseConds;
	}
	
	public ImmutableBitSet getConditions() {
		if (conditions == null) {
			conditions = trueConds.or(falseConds);
		}
		return conditions;
	}
	
	public ICall getDestination() {
		return destination;
	}
	
	public boolean hasKnowledge(int condition) {
		return trueConds.get(condition) || falseConds.get(condition);
	}
	
	public boolean getKnowledge(int condition) {
		if (!hasKnowledge(condition)) {
			throw new NoSuchElementException();
		}
		return trueConds.get(condition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((falseConds == null) ? 0 : falseConds.hashCode());
		result = prime * result + ((trueConds == null) ? 0 : trueConds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DecisionPathKnowledge)) {
			return false;
		}
		DecisionPathKnowledge other = (DecisionPathKnowledge) obj;
		if (destination != other.destination) {
			return false;
		}
		if (!Objects.equals(trueConds, other.trueConds)) {
			return false;
		}
		if (!Objects.equals(falseConds, other.falseConds)) {
			return false;
		}
		return true;
	}
}

package se.lth.cs.tycho.ir.entity.cal;


import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PrioritySpec extends AbstractIRNode {
	private final ImmutableList<String> scheduleStates;
	private final ImmutableList<Priority> priorities;

	public PrioritySpec(List<String> scheduleStates, List<Priority> priorities) {
		this(null, ImmutableList.from(scheduleStates), ImmutableList.from(priorities));
	}

	private PrioritySpec(PrioritySpec original, ImmutableList<String> scheduleStates, ImmutableList<Priority> priorities) {
		super(original);
		this.scheduleStates = scheduleStates;
		this.priorities = priorities;
	}

	public PrioritySpec copy(List<String> scheduleStates, List<Priority> priorities) {
		if (Lists.elementIdentityEquals(this.scheduleStates, scheduleStates) && Lists.elementIdentityEquals(this.priorities, priorities)) {
			return this;
		} else {
			return new PrioritySpec(this, ImmutableList.from(scheduleStates), ImmutableList.from(priorities));
		}
	}

	public ImmutableList<String> getScheduleStates() {
		return scheduleStates;
	}

	public PrioritySpec withScheduleStates(List<String> scheduleStates) {
		if (Lists.elementIdentityEquals(this.scheduleStates, scheduleStates)) {
			return this;
		} else {
			return new PrioritySpec(this, ImmutableList.from(scheduleStates), priorities);
		}
	}

	public ImmutableList<Priority> getPriorities() {
		return priorities;
	}

	public PrioritySpec withPriorities(List<Priority> priorities) {
		if (Lists.elementIdentityEquals(this.priorities, priorities)) {
			return this;
		} else {
			return new PrioritySpec(this, scheduleStates, ImmutableList.from(priorities));
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		priorities.forEach(action);
	}

	@Override
	public PrioritySpec transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(scheduleStates, (ImmutableList) priorities.map(transformation));
	}
}

package se.lth.cs.tycho.ir.entity.cal;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ActionCase extends AbstractIRNode {

	private final QID tag;
	private final ImmutableList<Action> actions;

	public ActionCase(QID tag, List<Action> actions) {
		this(null, tag, actions);
	}

	public ActionCase(IRNode original, QID tag, List<Action> actions) {
		super(original);
		this.tag = tag;
		this.actions = ImmutableList.from(actions);
	}

	public ActionCase copy(QID tag, List<Action> actions) {
		if (Objects.equals(getTag(), tag) && Lists.sameElements(getActions(), actions)) {
			return this;
		} else {
			return new ActionCase(this, tag, actions);
		}
	}

	public QID getTag() {
		return tag;
	}

	public ImmutableList<Action> getActions() {
		return actions;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		actions.forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(tag, transformation.mapChecked(Action.class, getActions()));
	}
}

package net.opendf.interp.preprocess;

import java.util.Map;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Transition;
import net.opendf.ir.am.util.ActorMachineUtils;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.PortName;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.transformers.AbstractTraverser;

public class SetChannelIds {
	private final ChannelIdSetter setter;

	public SetChannelIds() {
		setter = new ChannelIdSetter();
	}

	public void setChannelIds(ActorMachine actorMachine, Map<PortName, Integer> channels) {
		for (Scope s : actorMachine.getScopes()) {
			for (Decl d : s.getDeclarations()) {
				setter.traverseDecl(d, channels);
			}
		}
		for (Transition t : ActorMachineUtils.collectTransitions(actorMachine)) {
			setter.traverseStatements(t.getBody(), channels);
		}
		for (Expression c : ActorMachineUtils.collectPredicateConditionExpressions(actorMachine)) {
			setter.traverseExpression(c, channels);
		}
		for (PortCondition c : ActorMachineUtils.collectPortConditions(actorMachine)) {
			c.setChannelId(channels.get(c.getPortName()));
		}
	}

	private static class ChannelIdSetter extends AbstractTraverser<Map<PortName, Integer>> {
		@Override
		public Void visitExprInput(ExprInput in, Map<PortName, Integer> channels) {
			in.setChannelId(channels.get(in.getPort()));
			return super.visitExprInput(in, channels);
		}

		@Override
		public Void visitStmtOutput(StmtOutput out, Map<PortName, Integer> channels) {
			out.setChannelId(channels.get(out.getPort()));
			return super.visitStmtOutput(out, channels);
		}
	}

}

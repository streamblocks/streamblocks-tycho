package net.opendf.interp.preprocess;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExpressionVisitor;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.util.ImmutableList;

public class SetVariablePositions {
	private final MemAlloc memAlloc;
	private final StackAlloc stackAlloc;

	public SetVariablePositions() {
		memAlloc = new MemAlloc();
		stackAlloc = new StackAlloc();
	}

	public int setVariablePositions(ActorMachine actorMachine) {
		MemInfo memInfo = new MemInfo();
		for (ImmutableList<DeclVar> s : actorMachine.getScopes()) {
			for (Decl d : s) {
				memAlloc.traverseDecl(d, memInfo);
			}
		}
		for (Transition t : ActorMachineUtils.collectTransitions(actorMachine)) {
			memAlloc.traverseStatements(t.getBody(), memInfo);
		}
		for (Expression e : ActorMachineUtils.collectPredicateConditionExpressions(actorMachine)) {
			memAlloc.traverseExpression(e, memInfo);
		}

		return memInfo.size();
	}

	private abstract class AbstractTraverser<T> {
		public Void visitDeclVar(DeclVar d, T info) {return null;}
		public void traverseParDeclValue(ParDeclValue p, T info) {}
		public Void visitExprProc(ExprProc proc, T info) {return null;}
		public Void visitExprLambda(ExprLambda lambda, MemInfo info) {return null;}
	}
	
	private class MemAlloc extends AbstractTraverser<MemInfo> {
		@Override
		public Void visitDeclVar(DeclVar d, MemInfo info) {
			d.setVariablePosition(info.next(), false);
			super.visitDeclVar(d, info);
			return null;
		}

		@Override
		public void traverseParDeclValue(ParDeclValue p, MemInfo info) {
			throw new RuntimeException("Not supposed to be here...");
		}

		@Override
		public Void visitExprProc(ExprProc proc, MemInfo info) {
			stackAlloc.visitExprProc(proc, new StackInfo());
			return null;
		}

		@Override
		public Void visitExprLambda(ExprLambda lambda, MemInfo info) {
			stackAlloc.visitExprLambda(lambda, new StackInfo());
			return null;

		}
	}

	private class StackAlloc extends AbstractTraverser<StackInfo> {
		@Override
		public Void visitDeclVar(DeclVar d, StackInfo info) {
			d.setVariablePosition(info.next(), true);
			super.visitDeclVar(d, info);
			return null;
		}

		@Override
		public void traverseParDeclValue(ParDeclValue p, StackInfo info) {
			p.setVariablePosition(info.next(), true);
		}

		@Override
		public Void visitExprLet(ExprLet let, StackInfo info) {
			return super.visitExprLet(let, info.frame());
		}

		@Override
		public Void visitExprLambda(ExprLambda lambda, StackInfo info) {
			return super.visitExprLambda(lambda, info.frame());
		}

		@Override
		public Void visitExprProc(ExprProc proc, StackInfo info) {
			return super.visitExprProc(proc, info.frame());
		}

		@Override
		public Void visitStmtBlock(StmtBlock block, StackInfo info) {
			return super.visitStmtBlock(block, info.frame());
		}
	}

	private static class StackInfo {
		private int stackHeight;

		private StackInfo(int h) {
			stackHeight = h;
		}

		private StackInfo() {
			stackHeight = 0;
		}

		private int next() {
			return stackHeight++;
		}

		private StackInfo frame() {
			return new StackInfo(stackHeight);
		}
	}

	private static class MemInfo {
		private int memSize;

		private int next() {
			return memSize++;
		}

		private int size() {
			return memSize;
		}
	}

}

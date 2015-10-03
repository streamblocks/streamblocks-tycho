package se.lth.cs.tycho.phases;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.cfg.ActionBlock;
import se.lth.cs.tycho.cfg.Block;
import se.lth.cs.tycho.cfg.ConditionBlock;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.UniqueNumbers;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ProcessDescription;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ProcessToCalPhase implements Phase {
	@Override
	public String getDescription() {
		return "Translates process description to other Cal constructs.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		assert task.getSourceUnits().isEmpty();
		assert task.getTarget() != null;
		return task.withTarget(task.getTarget().withEntityDecls(
				task.getTarget().getEntityDecls().map(entityDecl -> entityDecl.withEntity(
						translate(entityDecl.getEntity(), context.getUniqueNumbers())))));
	}

	private Entity translate(Entity entity, UniqueNumbers uniqueNumbers) {
		if (!(entity instanceof CalActor)) {
			return entity;
		}
		CalActor actor = (CalActor) entity;
		if (actor.getProcessDescription() != null) {
			assert actor.getScheduleFSM() == null;
			assert actor.getActions().isEmpty(); // TODO lift this restriction
			assert actor.getInitializers().isEmpty(); // TODO lift this restriction
			assert actor.getPriorities().isEmpty(); // TODO lift this restriction

			Block entryBlock = processToBlock(actor.getProcessDescription());
			BlockToCal blockToCal = MultiJ.from(BlockToCal.class)
					.bind("uniqueNumbers").to(uniqueNumbers)
					.instance();
			blockToCal.process(entryBlock);
			return actor.copy(actor.getTypeParameters(), actor.getValueParameters(), actor.getTypeDecls(),
					actor.getVarDecls(), actor.getInputPorts(), actor.getOutputPorts(), ImmutableList.empty(),
					blockToCal.actions().build(),
					new ScheduleFSM(blockToCal.transitions().build(), blockToCal.initialState().get()), null,
					blockToCal.priorities().build(), actor.getInvariants());
		} else {
			return actor;
		}
	}

	private Block processToBlock(ProcessDescription process) {
		ActionBlock exit = new ActionBlock(ImmutableList.empty(), null);
		Block entry = parse(new LinkedList<>(process.getStatements()), exit);
		if (process.isRepeated()) {
			exit.setSuccessor(entry);
		}
		exit.replaceWith(entry);
		return entry;
	}



	private Block parse(LinkedList<Statement> statements, Block successor) {
		if (statements.isEmpty()) {
			return successor;
		}

		LinkedList<Statement> result = new LinkedList<>();
		boolean endsWithWrite = false;
		if (statements.getLast() instanceof StmtWrite) {
			result.addFirst(statements.removeLast());
			endsWithWrite = true;
		}
		while (!statements.isEmpty() && statements.getLast() instanceof StmtAssignment) {
			result.addFirst(statements.removeLast());
		}
		if (!endsWithWrite && !statements.isEmpty() && statements.getLast() instanceof StmtRead) {
			result.addFirst(statements.removeLast());
		}
		if (!result.isEmpty()) {
			Block b = new ActionBlock(result, successor);
			return parse(statements, b);
		}

		if (statements.getLast() instanceof StmtIf) {
			StmtIf cond = (StmtIf) statements.removeLast();
			Block thenBlock = parse(new LinkedList<>(((StmtBlock) cond.getThenBranch()).getStatements()), successor);
			Block elseBlock;
			if (cond.getElseBranch() != null) {
				elseBlock = parse(new LinkedList<>(((StmtBlock) cond.getElseBranch()).getStatements()), successor);
			} else {
				elseBlock = successor;
			}
			Block c = new ConditionBlock(cond.getCondition(), thenBlock, elseBlock);
			return parse(statements, c);
		}

		if (statements.getLast() instanceof StmtWhile) {
			StmtWhile whileStmt = (StmtWhile) statements.removeLast();
			ConditionBlock c = new ConditionBlock(whileStmt.getCondition(), null, successor);
			Block b = parse(new LinkedList<>(((StmtBlock) whileStmt.getBody()).getStatements()), c);
			c.setSuccessorIfTrue(b);
			return parse(statements, c);
		}

		throw new Error("Not implemented");
	}


	@Module
	interface BlockToCal {
		@Binding
		UniqueNumbers uniqueNumbers();

		@Binding
		default ImmutableList.Builder<Action> actions() {
			return ImmutableList.builder();
		}

		@Binding
		default ImmutableList.Builder<Transition> transitions() {
			return ImmutableList.builder();
		}

		@Binding
		default AtomicReference<String> initialState() {
			return new AtomicReference<>();
		}

		@Binding
		default ImmutableList.Builder<ImmutableList<QID>> priorities() {
			return ImmutableList.builder();
		}

		@Binding
		default Map<Block, String> blockNames() {
			return new HashMap<>();
		}

		default String blockName(Block block) {
			if (blockNames().containsKey(block)) {
				return blockNames().get(block);
			} else {
				String name = "block$" + blockNames().size();
				blockNames().put(block, name);
				return name;
			}
		}

		@Binding
		default Set<Block> processed() {
			return new HashSet<>();
		}

		default void process(Block block) {
			if (processed().add(block)) {
				initialState().compareAndSet(null, blockName(block));
				processBlock(block);
				block.forEachSuccessor(this::process);
			}
		}

		void processBlock(Block block);

		default void processBlock(ActionBlock block) {
			String curr = blockName(block);
			String succ = blockName(block.getSuccessor());
			transitions().add(new Transition(curr, succ, ImmutableList.of(QID.of(curr))));
			if (block.getStatements().isEmpty()) {
				actions().add(new Action(QID.of(curr), ImmutableList.empty(), ImmutableList.empty(),
						ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(),
						ImmutableList.empty(), null, ImmutableList.empty(), ImmutableList.empty()));
			} else {
				Statement first = block.getStatements().get(0);
				Statement last = block.getStatements().get(block.getStatements().size() - 1);
				if (first instanceof StmtRead) {
					StmtRead read = (StmtRead) first;
					ImmutableList<Map.Entry<LValue, VarDecl>> varDecls = read.getLValues()
							.map(lvalue -> ImmutableEntry.of(lvalue, VarDecl.local(null, "t$" + uniqueNumbers().next(), true, null)));
					InputPattern input = new InputPattern(read.getPort(), varDecls.map(Map.Entry::getValue), read.getRepeatExpression());
					ImmutableList.Builder<Statement> bodyBuilder = ImmutableList.builder();
					varDecls.forEach(entry -> bodyBuilder.add(new StmtAssignment(entry.getKey(),
							new ExprVariable(Variable.variable(entry.getValue().getName())))));
					Iterator<Statement> iterator = block.getStatements().iterator();
					iterator.next();
					iterator.forEachRemaining(bodyBuilder);
					actions().add(new Action(QID.of(curr), ImmutableList.of(input), ImmutableList.empty(),
							ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(),
							bodyBuilder.build(), null, ImmutableList.empty(), ImmutableList.empty()));
				} else if (last instanceof StmtWrite) {
					ImmutableList.Builder<Statement> bodyBuilder = ImmutableList.builder();
					for (int i = 0; i < block.getStatements().size() - 1; i++) {
						bodyBuilder.add(block.getStatements().get(i));
					}
					StmtWrite write = (StmtWrite) last;
					OutputExpression output = new OutputExpression(write.getPort(), write.getValues(), write.getRepeatExpression());
					actions().add(new Action(QID.of(curr), ImmutableList.empty(), ImmutableList.of(output),
							ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(),
							bodyBuilder.build(), null, ImmutableList.empty(), ImmutableList.empty()));
				} else {
					actions().add(new Action(QID.of(curr), ImmutableList.empty(), ImmutableList.empty(),
							ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(),
							block.getStatements(), null, ImmutableList.empty(), ImmutableList.empty()));
				}
			}

		}

		default void processBlock(ConditionBlock block) {
			String cond = blockName(block);
			String condNeg = cond + "neg";
			String thenBlock = blockName(block.getSuccessorIfTrue());
			String elseBlock = blockName(block.getSuccessorIfFalse());
			transitions().add(new Transition(cond, thenBlock, ImmutableList.of(QID.of(cond))));
			transitions().add(new Transition(cond, elseBlock, ImmutableList.of(QID.of(condNeg))));
			actions().add(new Action(QID.of(cond), ImmutableList.empty(), ImmutableList.empty(),
					ImmutableList.empty(), ImmutableList.empty(), ImmutableList.of(block.getCondition()),
					ImmutableList.empty(), null, ImmutableList.empty(), ImmutableList.empty()));
			actions().add(new Action(QID.of(condNeg), ImmutableList.empty(), ImmutableList.empty(),
					ImmutableList.empty(), ImmutableList.empty(), ImmutableList.empty(),
					ImmutableList.empty(), null, ImmutableList.empty(), ImmutableList.empty()));
			priorities().add(ImmutableList.of(QID.of(cond), QID.of(condNeg)));
		}
	}


}

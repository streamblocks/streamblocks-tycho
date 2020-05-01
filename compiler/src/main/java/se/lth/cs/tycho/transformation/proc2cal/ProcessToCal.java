package se.lth.cs.tycho.transformation.proc2cal;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.UniqueNumbers;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ProcessDescription;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class ProcessToCal {
	private ProcessToCal() {}

	public static CalActor translate(CalActor process, UniqueNumbers uniqueNumbers) {
		if (process.getProcessDescription() != null) {
			assert process.getScheduleFSM() == null;
			assert process.getActions().isEmpty();
			assert process.getInitializers().isEmpty();
			assert process.getPriorities().isEmpty();

			Block entryBlock = processToBlock(process.getProcessDescription()).current();
			BlockToCal blockToCal = MultiJ.from(BlockToCal.class)
					.bind("uniqueNumbers").to(uniqueNumbers)
					.instance();
			blockToCal.process(entryBlock);
			return process.copy(process.getTypeParameters(), process.getValueParameters(), process.getTypeDecls(),
					process.getVarDecls(), process.getInputPorts(), process.getOutputPorts(), ImmutableList.empty(),
					blockToCal.actions().build(), Collections.emptyList(),
					new ScheduleFSM(blockToCal.transitions().build(), blockToCal.initialState().get()), null,
					blockToCal.priorities().build(), process.getInvariants());
		} else {
			return process;
		}
	}

	private static Block processToBlock(ProcessDescription process) {
		ActionBlock exit = new ActionBlock(ImmutableList.empty(), null);
		Block entry = parse(new LinkedList<>(process.getStatements()), exit);
		if (process.isRepeated()) {
			exit.setSuccessor(entry);
			exit.replaceWith(entry);
		}
		return entry;
	}



	private static Block parse(LinkedList<Statement> statements, Block successor) {
		if (statements.isEmpty()) {
			return successor;
		}

		LinkedList<Statement> result = new LinkedList<>();
		boolean endsWithWrite = false;
		if (statements.getLast() instanceof StmtWrite) {
			result.addFirst(statements.removeLast());
			endsWithWrite = true;
		}
		while (!statements.isEmpty() && (statements.getLast() instanceof StmtAssignment || statements.getLast() instanceof StmtCall)) {
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
			Block thenBlock = parse(new LinkedList<>(cond.getThenBranch()), successor);
			Block elseBlock;
			if (cond.getElseBranch() != null) {
				elseBlock = parse(new LinkedList<>(cond.getElseBranch()), successor);
			} else {
				elseBlock = successor;
			}
			Block c = new ConditionBlock(cond.getCondition(), thenBlock, elseBlock);
			return parse(statements, c);
		}

		if (statements.getLast() instanceof StmtWhile) {
			StmtWhile whileStmt = (StmtWhile) statements.removeLast();
			ConditionBlock c = new ConditionBlock(whileStmt.getCondition(), null, successor);
			Block b = parse(new LinkedList<>(whileStmt.getBody()), c);
			c.setSuccessorIfTrue(b);
			return parse(statements, c);
		}

		if (statements.getLast() instanceof StmtBlock) {
			StmtBlock block = (StmtBlock) statements.removeLast();
			assert block.getVarDecls().isEmpty();
			return parse(new LinkedList<>(block.getStatements()), successor);
		}
		throw new Error("Not implemented");
	}


	@Module
	interface BlockToCal {
		@Binding(BindingKind.INJECTED)
		UniqueNumbers uniqueNumbers();

		@Binding(BindingKind.LAZY)
		default ImmutableList.Builder<Action> actions() {
			return ImmutableList.builder();
		}

		@Binding(BindingKind.LAZY)
		default ImmutableList.Builder<Transition> transitions() {
			return ImmutableList.builder();
		}

		@Binding(BindingKind.LAZY)
		default AtomicReference<String> initialState() {
			return new AtomicReference<>();
		}

		@Binding(BindingKind.LAZY)
		default ImmutableList.Builder<ImmutableList<QID>> priorities() {
			return ImmutableList.builder();
		}

		@Binding(BindingKind.LAZY)
		default Map<Block, String> blockNames() {
			return new HashMap<>();
		}

		default String blockName(Block block) {
			if (blockNames().containsKey(block)) {
				return blockNames().get(block);
			} else {
				String name = "block_" + blockNames().size();
				blockNames().put(block, name);
				return name;
			}
		}

		@Binding(BindingKind.LAZY)
		default Set<Block> processed() {
			return new HashSet<>();
		}

		default void process(Block block) {
			if (block == null) return;
			block = block.current();
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
					ImmutableList<Map.Entry<LValue, Match>> varDecls = read.getLValues()
							.map(lvalue -> ImmutableEntry.of(lvalue, match(VarDecl.input("t_" + uniqueNumbers().next()))));
					InputPattern input = new InputPattern(read.getPort(), varDecls.map(Map.Entry::getValue), read.getRepeatExpression());
					ImmutableList.Builder<Statement> bodyBuilder = ImmutableList.builder();
					varDecls.forEach(entry -> bodyBuilder.add(new StmtAssignment(entry.getKey(),
							new ExprVariable(Variable.variable(entry.getValue().getDeclaration().getName())))));
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

		default Match match(InputVarDecl decl) {
			Expression expression = new ExprVariable(Variable.variable(decl.getName()));
			ExprCase.Alternative alternative = new ExprCase.Alternative(new PatternBinding(new PatternVarDecl(decl.getName())), Collections.emptyList(), new ExprLiteral(ExprLiteral.Kind.True));
			ExprCase.Alternative otherwise = new ExprCase.Alternative(new PatternWildcard(), Collections.emptyList(), new ExprLiteral(ExprLiteral.Kind.False));
			ExprCase expr = new ExprCase(expression, Arrays.asList(alternative, otherwise));
			return new Match(decl, expr);
		}
	}
}

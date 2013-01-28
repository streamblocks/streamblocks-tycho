package net.opendf.interp;

import net.opendf.ir.am.ActorMachine;

public class Sim implements Simulator {

	private final ActorMachineRunner actorMachineRunner;
	private final Executor executor;
	private final Evaluator evaluator;
	private final Declarator declarator;
	private final Stack stack;
	private final TypeConverter converter;
	private final GeneratorFilterHelper generator;
	private final Environment actorMachineEnvironment;

	public Sim(ActorMachine actorMachine, Channel.InputEnd[] channelIn, Channel.OutputEnd[] channelOut, int memorySize,
			int stackSize) {
		actorMachineRunner = new BasicActorMachineRunner(this, actorMachine);
		executor = new StatementExecutor(this);
		evaluator = new ExpressionEvaluator(this);
		declarator = new VarDeclarator(this);
		stack = new BasicStack(stackSize);
		converter = new TypeConverter();
		generator = new GeneratorFilterHelper(this);
		actorMachineEnvironment = new BasicEnvironment(channelIn, channelOut, memorySize);
	}

	@Override
	public ActorMachineRunner actorMachineRunner() {
		return actorMachineRunner;
	}

	@Override
	public Executor executor() {
		return executor;
	}

	@Override
	public Evaluator evaluator() {
		return evaluator;
	}

	@Override
	public Declarator declarator() {
		return declarator;
	}

	@Override
	public Stack stack() {
		return stack;
	}

	@Override
	public TypeConverter converter() {
		return converter;
	}

	@Override
	public GeneratorFilterHelper generator() {
		return generator;
	}

	@Override
	public Environment actorMachineEnvironment() {
		return actorMachineEnvironment;
	}

}

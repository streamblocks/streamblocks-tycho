package net.opendf.interp;

public interface Simulator {
	public ActorMachineRunner actorMachineRunner();
	public Executor executor();
	public Evaluator evaluator();
	public Declarator declarator();
	public Stack stack();
	public TypeConverter converter();
	public GeneratorFilterHelper generator();
}

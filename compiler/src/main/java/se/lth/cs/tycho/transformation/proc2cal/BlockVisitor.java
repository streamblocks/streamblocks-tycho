package se.lth.cs.tycho.transformation.proc2cal;

public interface BlockVisitor<R, P> {
	R visitActionBlock(ActionBlock b, P p);
	R visitConditionBlock(ConditionBlock b, P p);
}

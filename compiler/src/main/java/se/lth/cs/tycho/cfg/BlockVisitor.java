package se.lth.cs.tycho.cfg;

public interface BlockVisitor<R, P> {
	R visitActionBlock(ActionBlock b, P p);
	R visitConditionBlock(ConditionBlock b, P p);
}

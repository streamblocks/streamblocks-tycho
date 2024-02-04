package streamblocks.tycho.simulator;

import se.lth.cs.tycho.ir.entity.am.*;
import se.lth.cs.tycho.ir.entity.am.ctrl.*;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class BasicActorMachineSimulator implements Simulator, InstructionVisitor<State, Environment>, ConditionVisitor<Boolean, Environment> {

    private final ActorMachine actorMachine;

    private final Environment environment;
    private final Interpreter interpreter;

    private final BitSet liveScopes;            // true if all variables in the scope is initialized, i.e. assigned to default values.
    private final Map<Condition, BitSet> condRequiredScope;   // for each actor machine condition, which scopes are required
    private final BitSet[] transRequiredScope;  // for each transition, which scopes are required

    private int state;

    public BasicActorMachineSimulator(ActorMachine actorMachine, Environment environment, Interpreter interpreter) {
        this.actorMachine = actorMachine;
        this.environment = environment;
        this.interpreter = interpreter;

        ImmutableList<Scope> scopeList = actorMachine.getScopes();
        int nbrScopes = scopeList.size();
        this.liveScopes = new BitSet(nbrScopes);
        // conditions, find the required scopes for each condition
        condRequiredScope = new HashMap<Condition, BitSet>(actorMachine.getConditions().size());

        transRequiredScope = new BitSet[actorMachine.getTransitions().size()];
    }

    @Override
    public Boolean visitInputCondition(PortCondition c, Environment environment) {
        return null;
    }

    @Override
    public Boolean visitOutputCondition(PortCondition c, Environment environment) {
        return null;
    }

    @Override
    public Boolean visitPredicateCondition(PredicateCondition c, Environment environment) {
        return null;
    }

    @Override
    public State visitExec(Exec t, Environment environment) {
        return null;
    }

    @Override
    public State visitTest(Test t, Environment environment) {
        Condition expr = actorMachine.getCondition(t.condition());
        boolean cond = expr.accept(this, environment);
        return cond ? t.targetTrue() : t.targetFalse();
    }

    @Override
    public State visitWait(Wait t, Environment environment) {
        return t.target();
    }

    @Override
    public boolean step() {
        return false;
    }

    @Override
    public void scopesToString(StringBuffer sb) {

    }
}

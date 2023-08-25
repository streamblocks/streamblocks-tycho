package se.lth.cs.tycho.transformation.cal2am;

import se.lth.cs.tycho.attribute.*;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.phase.TreeShadow;
import se.lth.cs.tycho.settings.Configuration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lth.cs.tycho.phase.ReduceActorMachinePhase.ReductionAlgorithm.ORDERED_CONDITION_CHECKING;
import static se.lth.cs.tycho.phase.ReduceActorMachinePhase.reductionAlgorithm;

public class CalToAm {
    protected final CalActor actor;
    private final EnumSet<KnowledgeRemoval.KnowledgeKind> onWait;
    private final EnumSet<KnowledgeRemoval.KnowledgeKind> onExec;
    private final Priorities priorities;
    private final Schedule schedule;

    private final Scopes scopes;
    private final Transitions transitions;
    private final Conditions conditions;
    private final Map<CalState, CalState> stateCache;

    // Flag indicating that the ORDERED_CONDITION_CHECKING reducer is being used. Flag is necessary as this reducer
    // also adds "Don't Care" conditions to some state values and sets them from unknown to false when they are
    // "Don't Care".
    boolean orderedConditions = false;

    public CalToAm(CalActor actor, Configuration configuration, ConstantEvaluator constants, Types types, TreeShadow tree, Ports ports, VariableDeclarations variableDecl, VariableScopes variableScopes, FreeVariables freeVar) {
        this.actor = actor;
        this.onWait = configuration.get(KnowledgeRemoval.forgetOnWait);
        this.onExec = configuration.get(KnowledgeRemoval.forgetOnExec);
        this.priorities = new Priorities(actor);
        this.schedule = new Schedule(actor);
        this.conditions = new Conditions(actor, constants, schedule);
        this.scopes = new Scopes(actor, constants, types, tree, variableDecl, variableScopes, freeVar);
        this.transitions = new Transitions(actor, constants, types, tree, scopes, ports, conditions, variableDecl, variableScopes, freeVar);
        this.stateCache = new HashMap<>();

        // If the reduction algorithm chosen is ORDERED_CONDITION_CHECKING then set orderedConditions flag to true.
        if(configuration.get(reductionAlgorithm).contains(ORDERED_CONDITION_CHECKING)) {
            orderedConditions = true;
        }
    }

    public ActorMachine buildActorMachine() {
        ActorMachine am = new ActorMachine(actor.getAnnotations(), actor.getInputPorts(), actor.getOutputPorts(), actor.getTypeParameters(), actor.getValueParameters(), scopes.getScopes(), new CalController(), transitions.getAllTransitions(), conditions.getAllConditions());
        am.setPosition(actor);
        return am;
    }

    private class CalController implements Controller {
        @Override
        public State getInitialState() {
            return cached(new CalState(schedule.getInitialState(), null, null, null));
        }
    }

    private CalState cached(CalState s) {
        return stateCache.computeIfAbsent(s, Function.identity());
    }

    private List<Condition> getConditionsOrderedByPriority(){
        List<Condition> conditionsCopy = new ArrayList<>(conditions.getAllConditions());
        conditionsCopy.sort(Condition::compareTo);
        return conditionsCopy;
    }

    public class CalState implements State {
        private List<Instruction> instructions;
        private final Set<String> state;
        private final Map<Port, PortKnowledge> inputPorts;
        private final Map<Port, PortKnowledge> outputPorts;
        private final Map<PredicateCondition, Boolean> predicateConditions;

        private List<Action> validActions;

        // Stores whether this state is fragmented or not. A state is fragmented if the conditions have not been
        // evaluated in the order they appear in the getConditionsOrderedByPriority list.  Eg c1,c2,3 = TFU is not
        // fragmented as c1 and c2 have been evaluated while c3 has not which is the expected order, but c1,c2,3 = TUF
        // is fragmented as c3 has been evaluated before c2.
        //boolean fragmented = false;

        // String containing all values of the conditions for the state. Eg: if c1,c2,c3 = true,false,unknown, the value
        // of this string is |TFU|.
        public String stateValue;

        public CalState(Set<String> state, Map<Port, PortKnowledge> inputPorts, Map<Port, PortKnowledge> outputPorts, Map<PredicateCondition, Boolean> predicateConditions) {
            this.state = state == null ? Collections.emptySet() : state;
            this.inputPorts = inputPorts == null ? Collections.emptyMap() : inputPorts;
            this.outputPorts = outputPorts == null ? Collections.emptyMap() : outputPorts;
            this.predicateConditions = predicateConditions == null ? Collections.emptyMap() : predicateConditions;

            /*
            // 1.  Determines the value of the fragmented flag.
            NOTE: Not making use of fragmentation information at the moment
            boolean prevKnowledgePresent = true;

            for (Condition condition: getConditionsOrderedByPriority()) {
                // 1.1 If a specific condition does not get checked within a specific FSM state, then it is not part
                // of the fragmentation check.
                if(Collections.disjoint(conditions.getStatesForCondition(condition), state)){
                    continue;
                }

                // 1.2 If the previous condition was UNKNOWN but the current conditions is known then the state is
                // fragmented.
                if(getConditionKnowledge(condition) == Knowledge.UNKNOWN && prevKnowledgePresent){
                    prevKnowledgePresent = false;
                }else if (getConditionKnowledge(condition) != Knowledge.UNKNOWN && !prevKnowledgePresent){
                    fragmented = true;
                    break;
                }
            }
            */

            // 2. Set state value
            stateValue = stateKnowledgeCompactString(this);
        }

        @Override
        public List<Instruction> getInstructions() {
            if (instructions == null) {
                instructions = computeInstructions();
            }
            return instructions;
        }

        private List<Instruction> computeInstructions() {
            List<Action> notDisabled = schedule.getEligibleActions(state).stream()
                    .filter(action -> inputConditions(action) != Knowledge.FALSE)
                    .filter(action -> predicateConditions(action) != Knowledge.FALSE)
                    .collect(Collectors.toList());

            validActions = notDisabled;

            Set<QID> selectedTags = notDisabled.stream().map(Action::getTag).collect(Collectors.toSet());

            Set<QID> prioritizedTags = priorities.getPrioritized(state, selectedTags);

            List<Action> highPrioNotDisabled = notDisabled.stream()
                    .filter(action -> prioritizedTags.contains(action.getTag()))
                    .collect(Collectors.toList());

            List<Instruction> execInstrucitons = highPrioNotDisabled.stream()
                    .filter(action -> inputConditions(action) == Knowledge.TRUE)
                    .filter(action -> predicateConditions(action) == Knowledge.TRUE)
                    .filter(action -> outputConditions(action) == Knowledge.TRUE)
                    .map(this::createExec)
                    .collect(Collectors.toList());

            if (!execInstrucitons.isEmpty()) {
                return execInstrucitons;
            }

            List<Action> testable = highPrioNotDisabled.stream()
                    .filter(action -> outputConditions(action) != Knowledge.FALSE)
                    .collect(Collectors.toList());

            validActions = testable;

            Stream<Instruction> inputTests = testable.stream()
                    .flatMap(action -> action.getInputPatterns().stream())
                    .filter(input -> portCondition(conditions.getCondition(input)) == Knowledge.UNKNOWN)
                    .map(this::createTest);

            Stream<Instruction> outputTests = testable.stream()
                    .flatMap(action -> action.getOutputExpressions().stream())
                    .filter(output -> portCondition(conditions.getCondition(output)) == Knowledge.UNKNOWN)
                    .map(this::createTest);

            Stream<Instruction> guardTests = testable.stream()
                    .flatMap(action -> action.getGuards().stream())
                    .filter(guard -> predicateCondition(conditions.getCondition(guard)) == Knowledge.UNKNOWN)
                    .map(this::createTest);

            List<Instruction> execOrTestInstrucitons = Stream.concat(execInstrucitons.stream(), Stream.concat(inputTests, Stream.concat(outputTests, guardTests))).collect(Collectors.toList());

            if (execOrTestInstrucitons.isEmpty()) {
                return Collections.singletonList(createWait());
            } else {
                return execOrTestInstrucitons;
            }
        }

        private Knowledge inputConditions(Action action) {
            Knowledge result = Knowledge.TRUE;
            for (InputPattern in : action.getInputPatterns()) {
                result = result.and(portCondition(conditions.getCondition(in)));
                if (result == Knowledge.FALSE) return result;
            }
            return result;
        }

        private Knowledge outputConditions(Action action) {
            Knowledge result = Knowledge.TRUE;
            for (OutputExpression out : action.getOutputExpressions()) {
                result = result.and(portCondition(conditions.getCondition(out)));
                if (result == Knowledge.FALSE) return result;
            }
            return result;
        }

        private Knowledge predicateConditions(Action action) {
            Knowledge result = Knowledge.TRUE;
            for (Expression guard : action.getGuards()) {
                result = result.and(predicateCondition(conditions.getCondition(guard)));
                if (result == Knowledge.FALSE) return result;
            }
            return result;
        }

        private Knowledge predicateCondition(PredicateCondition predicateCondition) {
            return Knowledge.ofNullable(predicateConditions.get(predicateCondition));
        }

        private Knowledge portCondition(PortCondition condition) {
            Map<Port, PortKnowledge> knowledge;
            if (condition.isInputCondition()) {
                knowledge = inputPorts;
            } else {
                knowledge = outputPorts;
            }
            return knowledge.getOrDefault(condition.getPortName(), PortKnowledge.nil()).has(condition.N());
        }

        private Knowledge getConditionKnowledge(PortCondition condition){
            return portCondition(condition);
        }

        private Knowledge getConditionKnowledge(PredicateCondition condition){
            return predicateCondition(condition);
        }

        private Knowledge getConditionKnowledge(Condition condition){
            if(condition instanceof PortCondition){
                return getConditionKnowledge((PortCondition) condition);
            } else if (condition instanceof PredicateCondition) {
                return getConditionKnowledge((PredicateCondition) condition);
            } else {
                throw new RuntimeException("Cannot call getConditionKnowledge(Condition condition) on condition of type: " + condition.getClass());
            }
        }

        private Exec createExec(Action action) {
            // If orderedConditions flag true, set all condition knowledge to null
            Map<Port, PortKnowledge> input;
            if (onExec.contains(KnowledgeRemoval.KnowledgeKind.INPUT) || orderedConditions) {
                input = null;
            } else {
                input = new HashMap<>(inputPorts);
                action.getInputPatterns().stream()
                        .map(conditions::getCondition)
                        .forEach(cond -> input.compute(cond.getPortName(), (port, k) -> k.add(-cond.N())));
            }
            Map<Port, PortKnowledge> output;
            if (onExec.contains(KnowledgeRemoval.KnowledgeKind.OUTPUT) || orderedConditions) {
                output = null;
            } else {
                output = new HashMap<>(outputPorts);
                action.getOutputExpressions().stream()
                        .map(conditions::getCondition)
                        .forEach(cond -> output.compute(cond.getPortName(), (port, k) -> k.add(-cond.N())));
            }
            Map<PredicateCondition, Boolean> guards;
            if (onExec.contains(KnowledgeRemoval.KnowledgeKind.GUARDS) || orderedConditions) {
                guards = null;
            } else {
                guards = null; // TODO: implement a more fine grained removal
            }


            CalState target = (cached(new CalState(schedule.targetState(state, action), input, output, guards)));
            return new Exec(transitions.getTransitionIndex(action), target);
        }

        private CalState withCondition(PortCondition condition, boolean value) {
            Map<Port, PortKnowledge> ports;
            if (condition.isInputCondition()) {
                ports = new HashMap<>(inputPorts);
            } else {
                ports = new HashMap<>(outputPorts);
            }
            PortKnowledge current = ports.getOrDefault(condition.getPortName(), PortKnowledge.nil());
            PortKnowledge next;
            if (value) {
                next = current.withLowerBound(condition.N());
            } else {
                next = current.withUpperBound(condition.N() - 1);
            }
            ports.put(condition.getPortName(), next);
            Map<Port, PortKnowledge> inputPorts = this.inputPorts;
            Map<Port, PortKnowledge> outputPorts = this.outputPorts;
            if (condition.isInputCondition()) {
                inputPorts = ports;
            } else {
                outputPorts = ports;
            }

            CalState target = (cached(new CalState(state, inputPorts, outputPorts, predicateConditions)));
            // If a test condition is false, find all DC conditions in the target and set them to false.
            if(value == false && orderedConditions) {
                target = setDontCareValues(condition, target);
            }

            return target;
        }

        private CalState withCondition(PredicateCondition condition, boolean value) {
            Map<PredicateCondition, Boolean> predicateConditions = new HashMap<>(this.predicateConditions);
            predicateConditions.put(condition, value);
            CalState target = (cached(new CalState(state, inputPorts, outputPorts, predicateConditions)));

            // If a test condition is false, find all DC conditions in the target and set them to false.
            if(value == false && orderedConditions){
                target = setDontCareValues(condition ,target);
            }

            return target;
        }

        private Test createTest(InputPattern input) {
            PortCondition portCondition = conditions.getCondition(input);
            int index = conditions.getConditionIndex(input);
            return new Test(index, withCondition(portCondition, true), withCondition(portCondition, false), portCondition.getOrderNumber());
        }

        private Test createTest(OutputExpression output) {
            PortCondition portCondition = conditions.getCondition(output);
            int index = conditions.getConditionIndex(output);
            return new Test(index, withCondition(portCondition, true), withCondition(portCondition, false), portCondition.getOrderNumber());
        }

        private Test createTest(Expression guard) {
            PredicateCondition condition = conditions.getCondition(guard);
            int index = conditions.getConditionIndex(guard);
            return new Test(index, withCondition(condition, true), withCondition(condition, false), condition.getOrderNumber());
        }

        /**
         * Print the state condition values. Used for debugging.
         */
        private void printStateKnowledge(CalState state){
            for (Condition condition: conditions.getAllConditions()) {
                int conditionIndex = condition.getOrderNumber();
                System.out.println("\t\t" + condition.getOrderNumber() + " " + state.getConditionKnowledge(condition));
            }
        }

        /**
         * Return a string of the state condition values.
         */
        private String stateKnowledgeCompactString(CalState state){
            List<String> stateValues = Arrays.asList(new String[conditions.getAllConditions().size()]);
            for (Condition condition: conditions.getAllConditions()) {
                //System.out.print("\t\t" + condition.getKnowledgePriority() + " " + state.getConditionKnowledge(condition));
                if(state.getConditionKnowledge(condition) == Knowledge.UNKNOWN){
                    stateValues.set(condition.getOrderNumber(), new String("U"));
                }else if(state.getConditionKnowledge(condition) == Knowledge.TRUE){
                    stateValues.set(condition.getOrderNumber(), new String("T"));
                }else if(state.getConditionKnowledge(condition) == Knowledge.FALSE){
                    stateValues.set(condition.getOrderNumber(), new String("F"));
                }
            }

            String toReturn = "|";
            for (String stateValue: stateValues) {
                toReturn = toReturn + stateValue + " |";
            }
            return toReturn;
        }

        public Wait createWait() {
            // If orderedConditions flag true, set all condition knowledge to null
            List<Action> temporarilyDisabled = schedule.getEligibleActions(state).stream()
                    .filter(action -> inputConditions(action) == Knowledge.FALSE || outputConditions(action) == Knowledge.FALSE)
                    .collect(Collectors.toList());
            BitSet waitingFor = new BitSet();
            temporarilyDisabled.stream()
                    .flatMap(action -> action.getInputPatterns().stream())
                    .mapToInt(conditions::getConditionIndex)
                    .forEach(waitingFor::set);
            temporarilyDisabled.stream()
                    .flatMap(action -> action.getOutputExpressions().stream())
                    .mapToInt(conditions::getConditionIndex)
                    .forEach(waitingFor::set);

            Map<Port, PortKnowledge> input;
            if (onWait.contains(KnowledgeRemoval.KnowledgeKind.INPUT)) {
                input = null;
            } else {
                input = withoutAbsenceKnowledge(inputPorts);
            }
            Map<Port, PortKnowledge> output;
            if (onWait.contains(KnowledgeRemoval.KnowledgeKind.OUTPUT)) {
                output = null;
            } else {
                output = withoutAbsenceKnowledge(outputPorts);
            }
            Map<PredicateCondition, Boolean> guards;
            if (onWait.contains(KnowledgeRemoval.KnowledgeKind.GUARDS)) {
                guards = null;
            } else {
                guards = predicateConditions;
            }
            CalState target = cached(new CalState(state, input, output, guards));
            return new Wait(target, waitingFor);
        }

        /* Function not in use
        private CalState defragment(CalState inState){
            // Remember some states depend on the
            if(inState.fragmented){
                Map<Port, PortKnowledge> input = new HashMap<>();
                Map<Port, PortKnowledge> output = new HashMap<>();
                Map<PredicateCondition, Boolean> guards = new HashMap<>();

                //int highestStateToSave = conditions.getAllConditions().size()-1;
                for(Condition condition: getConditionsOrderedByPriority()){
                    if(Collections.disjoint(conditions.getStatesForCondition(condition),inState.state)){
                        continue;
                    }

                    if(inState.getConditionKnowledge(condition) == Knowledge.UNKNOWN) {
                        //highestStateToSave = condition.getKnowledgePriority() - 1;
                        break;
                    }

                    if(condition instanceof PortCondition){
                        PortCondition portCondition = (PortCondition) condition;
                        if(portCondition.isInputCondition()){
                            Port port = portCondition.getPortName();
                            input.put(port, inState.inputPorts.get(port));
                        }else{
                            Port port = portCondition.getPortName();
                            output.put(port, inState.outputPorts.get(port));
                        }
                    }else if (condition instanceof  PredicateCondition){
                        PredicateCondition predicateCondition = (PredicateCondition) condition;
                        guards.put(predicateCondition, inState.predicateConditions.get(predicateCondition));
                    }

                }
                return inState; //cached(new CalState(inState.state, input, output, guards));
            }else {
                return inState;
            }
        }*/

        /**
         * Create a destination state based on the setting of an input condition to false. All Don't Care conditions
         * have been determined and have had their values set to false in this state.
         *
         * There are two conditions that can results in a condition being set to Don't Care. They are discussed in detail
         * in the paper: "Callanan, G and Gruian, F, Hardware and Software Generation from Large Actor Machines"
         *
         * @param inCondition Condition in current state (inState) that is going to become false in the transition
         * @param inState Current state the condition belongs to
         * @return New state transitioned to from inState when inCondition is false with all Dont Care conditions set
         * to false.
         */
        private CalState setDontCareValues(Condition inCondition, CalState inState){
            Map<Port, PortKnowledge> input = new HashMap<>(inState.inputPorts);
            Map<Port, PortKnowledge> output = new HashMap<>(inState.outputPorts);
            Map<PredicateCondition, Boolean> predicateConditions = new HashMap<>(inState.predicateConditions);

            // 1. Get all conditions where the actions covered by that condition are a subset of the actions covered by the inCondition and set them to false
            // as they no longer matter when the inCondition is false
            Set<Condition> dontCareConditions = new HashSet<>(conditions.getContainedActions(inCondition)); // All actions are contained
            for (Condition dontCareCondition: dontCareConditions) {
                if(dontCareCondition instanceof PredicateCondition){
                    predicateConditions.put((PredicateCondition)dontCareCondition, false);
                }

                if(dontCareCondition instanceof PortCondition){
                    PortCondition portCondition = (PortCondition) dontCareCondition;
                    Port port = portCondition.getPortName();

                    if(portCondition.isInputCondition()){
                        PortKnowledge next = PortKnowledge.nil().withUpperBound(0);
                        input.put(port, next);
                    }else {
                        PortKnowledge next = PortKnowledge.nil().withUpperBound(0);
                        output.put(port, next);
                    }
                }
            }
            CalState target = (cached(new CalState(inState.state, input, output, predicateConditions)));

            // 2. Look through all conditions that are true and see if all actions covered by the condition are already unable to fire
            // if so set the true condition to false as its value no longer matters
            //System.out.print("\t");
            List<Condition> conditionsByPriority = conditions.getAllConditionsInOrder();
            for (int i = conditionsByPriority.size() - 1; i>=0; i--) {
                Condition condition = conditionsByPriority.get(i);
                if(target.getConditionKnowledge(condition) == Knowledge.TRUE) {
                    //System.out.println(condition.getKnowledgePriority() + " ");
                    Set<Condition> commonConditions = conditions.getCommonActionConditions(condition);
                    //System.out.println(commonConditions);
                    boolean allFalse = true;
                    for (Condition commonCondition: commonConditions) {
                        //System.out.println(target.getConditionKnowledge(commonCondition));
                        if(commonCondition == condition){
                            continue;
                        }
                        if(target.getConditionKnowledge(commonCondition) != Knowledge.FALSE){
                            allFalse = false;
                            break;
                        }
                    }
                    if(allFalse){
                        // This condition is a dont care, so we set it to false
                        if(condition instanceof PredicateCondition){
                            predicateConditions.put((PredicateCondition)condition, false);
                        }

                        if(condition instanceof PortCondition){
                            PortCondition portCondition = (PortCondition) condition;
                            Port port = portCondition.getPortName();

                            if(portCondition.isInputCondition()){
                                PortKnowledge next = PortKnowledge.nil().withUpperBound(0);
                                input.put(port, next);
                            }else {
                                PortKnowledge next = PortKnowledge.nil().withUpperBound(0);
                                output.put(port, next);
                            }
                        }
                    }
                }
            }

            target = (cached(new CalState(inState.state, input, output, predicateConditions)));
            return target;
        }


        public Map<Port, PortKnowledge> withoutAbsenceKnowledge(Map<Port, PortKnowledge> knowledge) {
            Map<Port, PortKnowledge> result = new HashMap<>(knowledge);
            List<Port> ports = new ArrayList<>(result.keySet());
            for (Port port : ports) {
                PortKnowledge k = result.get(port);
                if (k.lowerBound() == 0) {
                    result.remove(port);
                } else {
                    result.put(port, k.withoutUpperBound());
                }
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CalState calState = (CalState) o;

            if (getActor() != calState.getActor())
                if (!state.equals(calState.state)) return false;
            if (!inputPorts.equals(calState.inputPorts)) return false;
            if (!outputPorts.equals(calState.outputPorts)) return false;
            return predicateConditions.equals(calState.predicateConditions);

        }

        public CalActor getActor() {
            return actor;
        }

        @Override
        public int hashCode() {
            int result = state.hashCode();
            result = 31 * result + inputPorts.hashCode();
            result = 31 * result + outputPorts.hashCode();
            result = 31 * result + predicateConditions.hashCode();
            return result;
        }

        public List<Action> getTestableActions(){
            if (validActions == null) {
                getInstructions();
                if(validActions == null){
                    throw new RuntimeException("We always expect valid actions to be initialised after getInstructions() called.");
                }
            }
            return validActions;
        }
    }
}

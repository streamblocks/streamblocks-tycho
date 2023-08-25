package se.lth.cs.tycho.transformation.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.*;

import java.util.function.Function;

/**
 * Reducer that reducers MultiInstructionState objects to SingleInstructionState objects based on the order numbers
 * assigned to each Test condition. When a MultiInstructionState object has many Test instructions, this reducer
 * examines each instruction's order number and selects the one with the lowest value as the single instruction for the
 * SingleInstructionState.
 *
 * If any of the instructions are Exec, then these are selected instead.
 */
public class SelectByConditionOrder implements Function<State, State> {

    public SelectByConditionOrder() {

    }

    @Override
    public State apply(State state) {

        // 1. If the state only has one attached instruction then this is the only one to return and no more
        // processing needs to occur
        if(state.getInstructions().size() == 1){
            return new SingleInstructionState(state.getInstructions().get(0));
        }

        // 2. Iterate through all instructions and if they are all Test instructions select the one with the lowest
        // order number. If one of the instructions is a non-test instruction, return that instruction instead.
        Instruction nextInstruction = null;
        int lowestOrderNumber = -1;

        for(Instruction instruction: state.getInstructions()){
            if(!(instruction instanceof Test)){
                if(instruction instanceof Wait){
                    throw new RuntimeException("We do not expect a Wait instruction when the State machine has more than one possible instruction.");
                }
                return new SingleInstructionState(instruction);
            }

            int instructionOrderNumber = ((Test) instruction).getOrderNumber();
            if(nextInstruction == null || lowestOrderNumber > instructionOrderNumber){
                nextInstruction = instruction;
                lowestOrderNumber = instructionOrderNumber;
            }
        }

        return new SingleInstructionState(nextInstruction);
    }
}

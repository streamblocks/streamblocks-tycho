package se.lth.cs.tycho.transformation.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.*;
import se.lth.cs.tycho.transformation.cal2am.CalToAm;
import se.lth.cs.tycho.util.TychoCollectors;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SingleActionTests implements Function<State, State> {

    public SingleActionTests() {

    }

    @Override
    public State apply(State state) {

        TransformedController.TransformedState transformedState =  (TransformedController.TransformedState) state;
        //System.out.println("Current index: " + transformedState.lowestActionIndex + ". Size: " + transformedState.getInstructions().size());

        if(state.getInstructions().size() == 1){
            Instruction instruction = state.getInstructions().get(0);
            /*if(instruction instanceof Test){
                Test testInstruction = (Test) instruction;
                TransformedController.TransformedState trueState = (TransformedController.TransformedState)testInstruction.targetTrue();
                TransformedController.TransformedState falseState = (TransformedController.TransformedState)testInstruction.targetFalse();
                System.out.println("\tTest: " + trueState.lowestActionIndex + " " + falseState.lowestActionIndex);
            } else if (instruction instanceof Exec) {
                Exec execInstruction = (Exec) instruction;
                TransformedController.TransformedState destState = (TransformedController.TransformedState) execInstruction.target();
                System.out.println("\tExec: " + destState.lowestActionIndex);
            } else if (instruction instanceof Wait) {
                Wait execInstruction = (Wait) instruction;
                TransformedController.TransformedState destState = (TransformedController.TransformedState) execInstruction.target();
                System.out.println("\tWait: " + destState.lowestActionIndex);
            }*/
            return new SingleInstructionState(state.getInstructions().get(0));
        }

        //System.out.println("Current index: " + transformedState.lowestActionIndex + ". Size: " + transformedState.getInstructions().size());

        Instruction nextPriorityInstruction = null;
        int nextPriorityInstructionActionIndex = -1;

        int currentActionIndex = transformedState.lowestActionIndex;
        for(Instruction instruction: state.getInstructions()){

            if(!(instruction instanceof Test)){
                if(instruction instanceof Wait){
                    throw new RuntimeException("We do not expect a Wait instruction when the State machine has more than one possible instruction.");
                }
                return new SingleInstructionState(instruction);
            }

            Test testInstruction = (Test) instruction;
            int trueStateActionIndex = ((TransformedController.TransformedState)testInstruction.targetTrue()).lowestActionIndex;
            int falseStateActionIndex = ((TransformedController.TransformedState)testInstruction.targetFalse()).lowestActionIndex;
            //System.out.println("\t" + trueStateActionIndex + " " + falseStateActionIndex);
            if(trueStateActionIndex == currentActionIndex){
                //System.out.println("\t\t\tNext Priority: " + trueStateActionIndex);
                return new SingleInstructionState(instruction);
            }else{

                if(nextPriorityInstruction == null){
                    nextPriorityInstruction = instruction;
                    nextPriorityInstructionActionIndex = trueStateActionIndex;
                    if(nextPriorityInstructionActionIndex < currentActionIndex){
                        nextPriorityInstructionActionIndex += transformedState.getTotalActions();
                    }
                }else{
                    if(trueStateActionIndex < currentActionIndex){
                        trueStateActionIndex += transformedState.getTotalActions();
                    }

                    if(trueStateActionIndex < nextPriorityInstructionActionIndex){
                        nextPriorityInstructionActionIndex = trueStateActionIndex;
                        nextPriorityInstruction = instruction;
                    }
                }
                // Here we find the best transition to the next state

            }

        }

        //Test testInstruction = (Test) nextPriorityInstruction;
        //int trueStateActionIndex = ((TransformedController.TransformedState)testInstruction.targetTrue()).lowestActionIndex;
        //System.out.println("\t\t\tDifferent Next Priority: " + trueStateActionIndex + " " + nextPriorityInstructionActionIndex);

        return new SingleInstructionState(nextPriorityInstruction);
    }
}

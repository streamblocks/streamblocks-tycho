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
        System.out.println("Current index: " + transformedState.lowestActionIndex + ". Size: " + transformedState.getInstructions().size());

        if(state.getInstructions().size() == 1){
            Instruction instruction = state.getInstructions().get(0);
            if(instruction instanceof Test){
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
            }
            return new SingleInstructionState(state.getInstructions().get(0));
        }

        for(Instruction instruction: state.getInstructions()){
            //System.out.println("\t\t" + (Test) instruction.getClass() );
            Test testInstruction = (Test) instruction;
            TransformedController.TransformedState trueState = (TransformedController.TransformedState)testInstruction.targetTrue();
            TransformedController.TransformedState falseState = (TransformedController.TransformedState)testInstruction.targetFalse();
            System.out.println("\t" + trueState.lowestActionIndex + " " + falseState.lowestActionIndex);
            /*if(trueState.lowestActionIndex == transformedState.lowestActionIndex){
                System.out.println("\t\t\tNext Priority: " + trueState.lowestActionIndex);
                return new SingleInstructionState(instruction);
            }*/

            //testInstruction.
        }

        return new SingleInstructionState(state.getInstructions().get(0));
    }
}

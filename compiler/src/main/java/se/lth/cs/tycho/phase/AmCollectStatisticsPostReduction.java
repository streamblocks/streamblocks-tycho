package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;
import se.lth.cs.tycho.transformation.cal2am.CalToAm;

import java.util.Collections;
import java.util.List;

public class AmCollectStatisticsPostReduction implements Phase {

    public static Setting<Boolean> printAMStatistics = new OnOffSetting() {
        @Override
        public String getKey() {
            return "print-am-statistics-post-reduction";
        }

        @Override
        public String getDescription() {
            return "Prints out useful statistics for the actor machines in CSV format. Takes place after actor " +
                    "machines have been reduced.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    public List<Setting<?>> getPhaseSettings() {
        return Collections.singletonList(printAMStatistics);
    }

    @Override
    public String getDescription() {
        return "Phase that prints out useful statistics for the actor machines. Takes place after actor machines" +
                " have been reduced.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        if (context.getConfiguration().get(printAMStatistics)) {
            System.out.println("AM Name,Reduced States,Reduced Conditions,Reduced Transitions,Reduced Scopes,Input Ports,Output Ports,Actions,Guards sum,Input Patterns sum,Output Expressions sum");
            GetAmStatistics statisticsGenerator = MultiJ.from(GetAmStatistics.class)
                    .bind("tree").to(task.getModule(TreeShadow.key))
                    .instance();
            statisticsGenerator.getAmStatistics(task);
        }
        return task;
    }

    @Module
    public interface GetAmStatistics {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        default void getAmStatistics(IRNode node) {
            node.forEachChild(this::getAmStatistics);
        }


        default void getAmStatistics(ActorMachine node) {
            String entityParentName = ((GlobalEntityDecl) tree().parent(node)).getName();
            System.out.print(entityParentName + "," + node.controller().getStateList().size() + "," + node.getConditions().size() + "," + node.getTransitions().size() + "," + node.getScopes().size() + ",");
            if(node.controller().getInitialState() instanceof CalToAm.CalState){
                CalActor actor = ((CalToAm.CalState) node.controller().getInitialState()).getActor();

                int totalGuards = 0;
                int totalInputPatterns = 0;
                int totalOutputExpressions = 0;
                for (Action action: actor.getActions()){
                    totalGuards += action.getGuards().size();
                    totalInputPatterns += action.getInputPatterns().size();
                    totalOutputExpressions += action.getOutputExpressions().size();
                }

                System.out.print(actor.getInputPorts().size() + "," + actor.getOutputPorts().size() + "," + actor.getActions().size() + "," + totalGuards + "," + totalInputPatterns + "," + totalOutputExpressions);
            }else{
                System.out.print("-1,-1,-1,-1,-1,-1");
            }

            System.out.println();
        }
    }
}


package se.lth.cs.tycho.phase;

import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;

public class AmCollectStatisticsPreReduction implements Phase {

    public static Setting<Boolean> printAMStatistics = new OnOffSetting() {
        @Override
        public String getKey() {
            return "print-am-statistics-pre-reduction";
        }

        @Override
        public String getDescription() {
            return "Prints out useful statistics for the actor machines in CSV format. Takes place before the actor " +
                    "machines have been reduced. Actor machines pre-reduction can be massive and counting their " +
                    "states will greatly increase compilation time and likely lead to compilation crashing for all " +
                    "but the smallest actor machines. Activate this phase with caution.";
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
        return "Phase that prints out useful statistics for the actor machines. Takes place before actor machines" +
                " have been reduced. Actor machines pre-reduction can be massive and counting their states will" +
                "greatly increase compilation time and will likely lead to compilation crashing for all but the" +
                "smallest actor machines. Activate this phase with caution.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        if (context.getConfiguration().get(printAMStatistics)) {
            System.out.println("AM Name,Unreduced States,Unreduced Conditions,Unreduced Transitions,Unreduced Scopes,Input Ports,Output Ports,Actions,Guards sum, Input Patterns sum, Output Expressions sum");
            AmCollectStatisticsPostReduction.GetAmStatistics statisticsGenerator =
                    MultiJ.from(AmCollectStatisticsPostReduction.GetAmStatistics.class)
                    .bind("tree").to(task.getModule(TreeShadow.key))
                    .instance();
            statisticsGenerator.getAmStatistics(task);
        }
        return task;
    }
}

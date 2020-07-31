package se.lth.cs.tycho.platform;

import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.settings.Setting;

import java.util.List;
import java.util.stream.Collectors;

public interface Platform {
    String name();

    String description();

    List<Phase> phases();

    default List<Setting<?>> settingsManager() {
        return phases()
                .stream()
                .flatMap(phase -> phase.getPhaseSettings().stream())
                .collect(Collectors.toList());
    }
}

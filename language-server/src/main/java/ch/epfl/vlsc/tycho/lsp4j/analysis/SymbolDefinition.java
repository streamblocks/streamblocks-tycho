package ch.epfl.vlsc.tycho.lsp4j.analysis;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

import java.util.Objects;

public final class SymbolDefinition {
    private final String name;
    private final SymbolType type;
    private final Location location;
    private final Range selectionRange;
    private final String containerName;

    public SymbolDefinition(String name, SymbolType type, Location location, Range selectionRange, String containerName) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.selectionRange = selectionRange;
        this.containerName = containerName;
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public Range getSelectionRange() {
        return selectionRange;
    }

    public String getContainerName() {
        return containerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SymbolDefinition)) {
            return false;
        }
        SymbolDefinition that = (SymbolDefinition) o;
        return Objects.equals(name, that.name)
                && type == that.type
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, location);
    }
}

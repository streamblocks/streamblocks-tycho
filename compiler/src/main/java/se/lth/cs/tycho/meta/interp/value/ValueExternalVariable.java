package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class ValueExternalVariable implements Value {

    private final String name;

    public ValueExternalVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueExternalVariable asValue = (ValueExternalVariable) o;
        return this.name.equals(asValue.getName());
    }
}

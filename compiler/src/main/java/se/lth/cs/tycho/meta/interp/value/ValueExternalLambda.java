package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class ValueExternalLambda implements Value {

    private final String functionName;
    private final ImmutableList<Value> args;
    public ValueExternalLambda(String functionName, ImmutableList<Value> args) {
        this.functionName = functionName;
        this.args = args;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ImmutableList<Value> getArgs() {
        return args;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueExternalLambda asValue = (ValueExternalLambda) o;
        return this.functionName.equals(asValue.getFunctionName()) && Lists.sameElements(args, asValue.getArgs());
    }
}

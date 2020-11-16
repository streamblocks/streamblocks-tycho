package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.Variable;


public class ValueNumber {

    private final String original;
    private final int valueNumber;


    private ValueNumber(String original, int valueNumber) {
        this.original = original;
        this.valueNumber = valueNumber;
    }

    public ValueNumber(String original){
        this(original, 0);
    }

    public ValueNumber getNextValueNumber(){
        return new ValueNumber(this.getOriginal(), this.getValueNumber()+1);
    }

    public String getOriginal() {
        return original;
    }

    public int getValueNumber() {
        return valueNumber;
    }

}

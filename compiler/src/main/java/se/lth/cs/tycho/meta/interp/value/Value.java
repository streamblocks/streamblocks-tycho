package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.type.TypeExpr;

public abstract class Value {
    protected TypeExpr type;
    public TypeExpr getType(){
        return type;
    }
    public void setType(TypeExpr type){
        this.type = type;
    }
}
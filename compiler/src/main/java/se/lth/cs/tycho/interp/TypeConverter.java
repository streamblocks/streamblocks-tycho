package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.*;
import se.lth.cs.tycho.reporting.CompilationException;

public enum TypeConverter {
    instance;

    public static TypeConverter getInstance() {
        return instance;
    }

    public boolean getBoolean(RefView r) throws CompilationException { return r.getBoolean();  }
    public void setBoolean(Ref r, boolean v) { r.setBoolean(v); }

    public Function getFunction(RefView r) throws CompilationException { return (Function) r.getValue(); }
    public void setFunction(Ref r, Function v) { r.setValue(v); }

    public Procedure getProcedure(RefView r) throws CompilationException { return (Procedure) r.getValue(); }
    public void setProcedure(Ref r, Procedure v) { r.setValue(v); }

    public List getList(RefView r) throws CompilationException { return (List) r.getValue(); }
    public void setList(Ref r, List v) { r.setValue(v); }

    public Collection getCollection(RefView r) throws CompilationException { return (Collection) r.getValue(); }
    public void setCollection(Ref r, Collection v) { r.setValue(v); }

    public int getInt(RefView r) throws CompilationException { return (int) r.getLong(); }
    public void setInt(Ref r, int v) { r.setLong(v); }

    public double getDouble(RefView r) throws CompilationException { return (int) r.getDouble(); }
    public void setDouble(Ref r, double v) { r.setDouble(v); }

}

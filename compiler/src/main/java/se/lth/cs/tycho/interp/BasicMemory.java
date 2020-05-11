package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;
import se.lth.cs.tycho.interp.values.BasicRef;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.HashMap;
import java.util.Map;

public class BasicMemory implements Memory {

    Map<VarDecl, Ref> localVarDecls;
    Map<VarDecl, Ref> globalVarDecls;

    public BasicMemory() {
        localVarDecls = new HashMap<>();
        globalVarDecls = new HashMap<>();
    }

    @Override
    public Ref getLocal(VarDecl var) {
        return localVarDecls.get(var);
    }

    @Override
    public Ref getGlobal(VarDecl var) {
        return globalVarDecls.get(var);
    }

    @Override
    public void declareLocal(VarDecl var) {
        localVarDecls.put(var, new BasicRef());
    }

    @Override
    public void declareGlobal(VarDecl var) {
        globalVarDecls.put(var, new BasicRef());
    }

}

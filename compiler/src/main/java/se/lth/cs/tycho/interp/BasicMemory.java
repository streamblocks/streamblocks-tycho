package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;
import se.lth.cs.tycho.interp.values.BasicRef;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;

import java.util.HashMap;
import java.util.Map;

public class BasicMemory implements Memory {

    Map<LocalVarDecl, Ref> localVarDecls;
    Map<GlobalVarDecl, Ref> globalVarDecls;

    public BasicMemory(){
        localVarDecls = new HashMap<>();
        globalVarDecls = new HashMap<>();
    }

    @Override
    public Ref get(LocalVarDecl var) throws InterpIndexOutOfBoundsException {
        return localVarDecls.get(var);
    }

    @Override
    public Ref get(GlobalVarDecl var) throws InterpIndexOutOfBoundsException {
        return globalVarDecls.get(var);
    }

    @Override
    public void declareLocal(LocalVarDecl var) {
        localVarDecls.put(var, new BasicRef());
    }

    @Override
    public void declareGlobal(GlobalVarDecl var) {
        globalVarDecls.put(var, new BasicRef());
    }

}

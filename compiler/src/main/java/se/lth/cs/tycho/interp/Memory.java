package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;

public interface Memory {
    Ref get(LocalVarDecl var) throws InterpIndexOutOfBoundsException;
    Ref get(GlobalVarDecl var) throws InterpIndexOutOfBoundsException;

    void declareLocal(LocalVarDecl var);
    void declareGlobal(GlobalVarDecl var);
}

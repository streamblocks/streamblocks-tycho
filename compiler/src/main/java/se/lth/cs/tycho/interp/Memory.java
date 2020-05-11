package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

public interface Memory {
    Ref getLocal(VarDecl var);
    Ref getGlobal(VarDecl var);

    void declareLocal(VarDecl var);
    void declareGlobal(VarDecl var);
}

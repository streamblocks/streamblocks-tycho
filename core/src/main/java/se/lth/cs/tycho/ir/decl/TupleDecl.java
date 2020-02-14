package se.lth.cs.tycho.ir.decl;

import java.util.List;

public class TupleDecl extends TaggedTupleDecl {

    public TupleDecl(List<FieldVarDecl> fields) {
        super(null, fields);
    }
}

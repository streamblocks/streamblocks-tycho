package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;

/**
 * 
 * @author Per Andersson
 * 
 */

public interface StructureStatement extends IRNode {
    <R, P> R accept(StructureStmtVisitor<R, P> v, P p);
}

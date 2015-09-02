package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.ExpressionVisitor;

public interface CodeVisitor<P> extends ExpressionVisitor<Object, P> {

}
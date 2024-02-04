package se.lth.cs.tycho.ir.entity.am.ctrl;

import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;

/**
 *
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */
public interface ConditionVisitor <R,P> {

    public R visitInputCondition(PortCondition c, P p);
    public R visitOutputCondition(PortCondition c, P p);
    public R visitPredicateCondition(PredicateCondition c, P p);

}

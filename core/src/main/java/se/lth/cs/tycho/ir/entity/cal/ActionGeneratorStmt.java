package se.lth.cs.tycho.ir.entity.cal;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

/**
 * Class representing an ActionGeneratorStatement.
 *
 * Action generator statements allow for a single action description within an actor to generate multiple similar
 * actions in the AST. That single action can be seen as a template generating many actions.
 *
 * The following code:
 * <pre>
 * foreach uint i in 0..3, i!=-1, i<3 generate // The generate statement
 *     testAction: action In[i]:[token] ==> Out[i]:[token] end
 * end
 * <pre/>
 * is mapped to the following nodes in this class:
 * - generator: uint i in 0..3
 * - filters: i!=-1, i<3
 * - actions: testAction
 *
 * Any action case statements would be stored in the 'actionCases' list and the 'actionGeneratorStmts' allows for
 * these action generators to be nested.
 *
 * The documentation in the ActionGeneratorEnumeration phase describes most of the syntax for the class.
 * Refer there for more information.
 */
public class ActionGeneratorStmt extends AbstractIRNode {

    private final Generator generator;
    private final ImmutableList<Expression> filters;
    private final ImmutableList<Action> actions;
    private final ImmutableList<ActionCase> actionCases;
    private final ImmutableList<ActionGeneratorStmt> actionGeneratorStmts;

    public ActionGeneratorStmt(Generator generator, List<Expression> filters, List<Action> actions,
                               List<ActionCase> actionCases, List<ActionGeneratorStmt> actionGeneratorStmts) {
        this(null, generator, filters, actions, actionCases, actionGeneratorStmts);
    }

    private ActionGeneratorStmt(ActionGeneratorStmt original, Generator generator, List<Expression> filters,
                                List<Action> actions, List<ActionCase> actionCases,
                                List<ActionGeneratorStmt> actionGeneratorStmts) {
        super(original);
        this.generator = generator;
        this.filters = ImmutableList.from(filters);
        this.actions = ImmutableList.from(actions);
        this.actionCases = ImmutableList.from(actionCases);
        this.actionGeneratorStmts = ImmutableList.from(actionGeneratorStmts);
    }

    public ActionGeneratorStmt copy(Generator generator, List<Expression> filters, List<Action> actions,
                                    List<ActionCase> actionCases, List<ActionGeneratorStmt> actionGeneratorStmts) {
        if (this.generator == generator && Lists.sameElements(this.filters, filters)
                && Lists.sameElements(this.actions, actions) && Lists.sameElements(this.actionCases, actionCases)
                && Lists.sameElements(this.actionGeneratorStmts, actionGeneratorStmts)) {
            return this;
        }
        return new ActionGeneratorStmt(this, generator, filters, actions, actionCases, actionGeneratorStmts);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (generator != null) action.accept(generator); // Do not expect this to be null, check is for consistency
        actions.forEach(action);
        actionCases.forEach(action);
        actionGeneratorStmts.forEach(action);
        filters.forEach(action);
    }

    @Override
    public IRNode transformChildren(Transformation transformation) {
        return copy(
            generator == null ? null: (Generator) transformation.apply(generator),
            (ImmutableList) filters.map(transformation),
            (ImmutableList) actions.map(transformation),
            (ImmutableList) actionCases.map(transformation),
            (ImmutableList) actionGeneratorStmts.map(transformation)
        );
    }

    public Generator getGenerator() {
        return generator;
    }
    public ImmutableList<Expression> getFilters() {
        return filters;
    }
    public ImmutableList<Action> getActions() {
        return actions;
    }
    public ImmutableList<ActionCase> getActionCases() {
        return actionCases;
    }
    public ImmutableList<ActionGeneratorStmt> getActionGeneratorStmts() {
        return actionGeneratorStmts;
    }
}

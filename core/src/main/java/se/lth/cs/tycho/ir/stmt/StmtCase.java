package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StmtCase extends Statement {

    public static class Alternative extends AbstractIRNode {

        private Pattern pattern;
        private ImmutableList<Expression> guards;
        private ImmutableList<Statement> statements;
        private ImmutableList<Annotation> annotations;


        public Alternative(Pattern pattern, List<Expression> guards, List<Statement> statements) {
            this(null, ImmutableList.empty(), pattern, guards, statements);
        }

        public Alternative(List<Annotation> annotations, Pattern pattern, List<Expression> guards, List<Statement> statements) {
            this(null, annotations, pattern, guards, statements);
        }

        public Alternative(IRNode original, List<Annotation> annotations, Pattern pattern, List<Expression> guards, List<Statement> statements) {
            super(original);
            this.annotations = ImmutableList.from(annotations);
            this.pattern = pattern;
            this.guards = ImmutableList.from(guards);
            this.statements = ImmutableList.from(statements);
        }

        public Pattern getPattern() {
            return pattern;
        }

        public ImmutableList<Expression> getGuards() {
            return guards;
        }

        public ImmutableList<Statement> getStatements() {
            return statements;
        }

        public ImmutableList<Annotation> getAnnotations() {
            return annotations;
        }

        public Alternative copy(List<Annotation> annotations, Pattern pattern, List<Expression> guards, List<Statement> statements) {
            if (Objects.equals(this.annotations, annotations) && Objects.equals(getPattern(), pattern) && Lists.sameElements(getGuards(), guards) && Lists.sameElements(getStatements(), statements)) {
                return this;
            } else {
                return new Alternative(this, annotations, pattern, guards, statements);
            }
        }

        @Override
        public void forEachChild(Consumer<? super IRNode> action) {
            annotations.forEach(action);
            action.accept(getPattern());
            guards.forEach(action);
            statements.forEach(action);
        }

        @Override
        public IRNode transformChildren(Transformation transformation) {
            return copy(annotations, (Pattern) transformation.apply(getPattern()), transformation.mapChecked(Expression.class, getGuards()), transformation.mapChecked(Statement.class, getStatements()));
        }
    }

    private Expression scrutinee;
    private ImmutableList<Alternative> alternatives;
    private ImmutableList<Annotation> annotations;

    public StmtCase(Expression scrutinee, List<Alternative> alternatives) {
        this(null, ImmutableList.empty(), scrutinee, alternatives);
    }

    public StmtCase(List<Annotation> annotations, Expression scrutinee, List<Alternative> alternatives) {
        this(null, annotations, scrutinee, alternatives);
    }

    public StmtCase(Statement original, List<Annotation> annotations, Expression scrutinee, List<Alternative> alternatives) {
        super(original);
        this.annotations = ImmutableList.from(annotations);
        this.scrutinee = scrutinee;
        this.alternatives = ImmutableList.from(alternatives);
    }

    public Expression getScrutinee() {
        return scrutinee;
    }

    public ImmutableList<Alternative> getAlternatives() {
        return alternatives;
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    public StmtCase copy(List<Annotation> annotations, Expression scrutinee, List<Alternative> alternatives) {
        if (Objects.equals(this.annotations, annotations) && Objects.equals(getScrutinee(), scrutinee) && Lists.sameElements(getAlternatives(), alternatives)) {
            return this;
        } else {
            return new StmtCase(this, annotations, scrutinee, alternatives);
        }
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
        action.accept(getScrutinee());
        getAlternatives().forEach(action);
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return copy(annotations, (Expression) transformation.apply(getScrutinee()), transformation.mapChecked(Alternative.class, getAlternatives()));
    }
}

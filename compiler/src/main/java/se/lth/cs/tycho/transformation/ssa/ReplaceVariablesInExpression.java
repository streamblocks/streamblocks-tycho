package se.lth.cs.tycho.transformation.ssa;

import org.multij.Module;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

@Module
public interface ReplaceVariablesInExpression {

    default Expression replaceVariables(Expression expr, SSABlock block) {
        return expr;
    }

    default Expression replaceVariables(ExprVariable expr, SSABlock block) {
        Variable var = expr.getVariable();
        return (Expression) block.readVariable(var).deepClone();
    }

    default Expression replaceVariables(ExprBinaryOp expr, SSABlock block) {
        List<Expression> newOp = expr.getOperands().stream().map(e -> replaceVariables(e, block)).collect(Collectors.toList());
        return new ExprBinaryOp(expr.getOperations(), ImmutableList.from(newOp));
    }

}

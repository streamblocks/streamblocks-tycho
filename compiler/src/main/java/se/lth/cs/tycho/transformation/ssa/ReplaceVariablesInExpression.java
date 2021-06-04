package se.lth.cs.tycho.transformation.ssa;

import org.multij.Module;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Module
public interface ReplaceVariablesInExpression {

    default Expression replaceVariables(Expression expr, SSABlock block) {
        return expr;
    }

    default Expression replaceVariables(ExprVariable expr, SSABlock block) {
        Variable var = expr.getVariable();
        return block.readVariable(var);
    }

    default Expression replaceVariables(ExprBinaryOp expr, SSABlock block) {
        List<Expression> newOp = expr.getOperands().stream().map(e -> replaceVariables(e, block)).collect(Collectors.toList());
        return new ExprBinaryOp(expr.getOperations(), ImmutableList.from(newOp));
    }

    default Expression replaceVariables(ExprIf iff, SSABlock block) {
        Expression cond = replaceVariables(iff.getCondition(), block);
        Expression then = replaceVariables(iff.getThenExpr(), block);
        Expression elze = replaceVariables(iff.getElseExpr(), block);
        return new ExprIf(cond, then, elze);
    }

    default Expression replaceVariables(ExprApplication app, SSABlock block) {
        List<Expression> args = new LinkedList<>(app.getArgs());
        args.replaceAll(arg -> replaceVariables(arg, block));
        return new ExprApplication(app.getFunction(), ImmutableList.from(args));
    }

    default Expression replaceVariables(ExprComprehension comp, SSABlock block) {
        Expression collection = replaceVariables(comp.getCollection(), block);
        List<Expression> filters = comp.getFilters();
        filters.replaceAll(f -> replaceVariables(f, block));
        return comp.copy(comp.getGenerator(), filters, collection).deepClone();
    }

    default Expression replaceVariables(ExprDeref deref, SSABlock block) {
        return deref.withReference(replaceVariables(deref.getReference(), block)).deepClone();
    }

    default Expression replaceVariables(ExprLambda lambda, SSABlock block) {
        return lambda.copy(lambda.getValueParameters(), replaceVariables(lambda.getBody(), block), lambda.getReturnType()).deepClone();
    }

    default Expression replaceVariables(ExprList list, SSABlock block) {
        List<Expression> elems = list.getElements();
        elems.replaceAll(e -> replaceVariables(e, block));
        return list.withElements(elems).deepClone();
    }

    default Expression replaceVariables(ExprSet set, SSABlock block) {
        List<Expression> elems = set.getElements();
        elems.replaceAll(e -> replaceVariables(e, block));
        return set.withElements(elems).deepClone();
    }

    default Expression replaceVariables(ExprTuple tuple, SSABlock block) {
        List<Expression> elems = tuple.getElements();
        elems.replaceAll(e -> replaceVariables(e, block));
        return tuple.copy(elems).deepClone();
    }

    default Expression replaceVariables(ExprTypeConstruction typeConstruction, SSABlock block) {
        List<Expression> elems = typeConstruction.getArgs();
        elems.replaceAll(e -> replaceVariables(e, block));
        return typeConstruction.copy(typeConstruction.getConstructor(), typeConstruction.getTypeParameters(), typeConstruction.getValueParameters(), elems).deepClone();
    }

    default Expression replaceVariables(ExprUnaryOp unOp, SSABlock block) {
        return unOp.copy(unOp.getOperation(), replaceVariables(unOp.getOperand(), block)).deepClone();
    }

    default Expression replaceVariables(ExprField field, SSABlock block) {
        return field.copy(replaceVariables(field.getStructure(), block), field.getField()).deepClone();
    }

    default Expression replaceVariables(ExprNth nth, SSABlock block) {
        return nth.copy(replaceVariables(nth.getStructure(), block), nth.getNth()).deepClone();
    }

    default Expression replaceVariables(ExprTypeAssertion exprTypeAssertion, SSABlock block) {
        return exprTypeAssertion.copy(replaceVariables(exprTypeAssertion.getExpression(), block), exprTypeAssertion.getType()).deepClone();
    }

    default Expression replaceVariables(ExprIndexer indexer, SSABlock block) {
        Expression newStruct = replaceVariables(indexer.getStructure(), block);
        Expression newIndex = replaceVariables(indexer.getIndex(), block);
        return indexer.copy(newStruct, newIndex).deepClone();
    }

    /*default Expression replaceVariables(ExprLet let, SSABlock block) {
        List<LocalVarDecl> lvd = let.getVarDecls();
        List<LocalVarDecl> newLvd = lvd.stream().map(lv -> getLocalVarDecl(lv.getOriginalName(), replacements.keySet())).collect(Collectors.toList());
        return let.withVarDecls(newLvd).deepClone();
    }*/

    default Expression replaceVariables(ExprMap map, SSABlock block) {
        ImmutableList<ImmutableEntry<Expression, Expression>> mappings = map.getMappings();
        ImmutableList<ImmutableEntry<Expression, Expression>> newMappings = ImmutableList.empty();

        for (ImmutableEntry<Expression, Expression> entry : mappings) {
            ImmutableEntry<Expression, Expression> newEntry = ImmutableEntry.of(replaceVariables(entry.getKey(), block), replaceVariables(entry.getValue(), block));
            newMappings.add(newEntry);
        }
        return map.withMappings(newMappings).deepClone();
    }

}

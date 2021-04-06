package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;

import java.util.*;
import java.util.function.Consumer;

public class SSABlock extends Statement {

    private List<TypeDecl> typeDecls;
    private LinkedList<LocalVarDecl> varDecls;
    private final List<SSABlock> predecessors = new LinkedList<>();
    private final Map<Variable, Expression> currentDef = new HashMap<>();
    private final Map<Variable, Integer> currentNumber = new HashMap<>();
    private List<Statement> statements;
    private List<StmtPhi> phis;
    private boolean sealed = false;

    private SSABlock(SSABlock original, List<TypeDecl> typeDecls, LinkedList<LocalVarDecl> varDecls) {
        super(original);
        this.typeDecls = typeDecls;
        this.varDecls = varDecls;
    }

    public SSABlock(List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls) {
        this(null, typeDecls, new LinkedList<>(varDecls));
    }

    public SSABlock() {
        this(new LinkedList<>(), new LinkedList<>());
    }

    public void seal() {
        sealed = true;
    }

    public LocalVarDecl getOriginalDefinition(Variable variable) {
        //varDecls.stream().fin
        return null;
    }

    public int getVariableNumber(Variable variable) {
        if (currentNumber.containsKey(variable)) {
            return currentNumber.get(variable);
        }
        return predecessors.stream().map(blk -> blk.getVariableNumber(variable)).max(Integer::compare).get();
    }

    public void incrementVariableNumber(Variable variable) {
        currentNumber.put(variable, getVariableNumber(variable) + 1);
    }

    public void addDecl(LocalVarDecl decl) {
        varDecls.add(decl);
    }

    public void addPredecessor(SSABlock pred) {
        predecessors.add(pred);
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public void writeVariable(Variable variable, Expression value) {
        currentDef.put(variable, value);
    }

    public Expression readVariable(Variable variable) {
        if (currentDef.containsKey(variable)) {
            return currentDef.get(variable);
        }
        return readVariableRecursive(variable);
    }

    public Expression readVariableRecursive(Variable variable) {
        throw new UnsupportedOperationException();
    }

    public SSABlock fill(List<Statement> statements) {

        //int splitIndex = 0;
        LinkedList<Statement> stmtsIter = new LinkedList<>(statements);
        ListIterator<Statement> it = stmtsIter.listIterator();
        while(it.hasNext()) {
            Statement statement = it.next();

            if (statement instanceof StmtAssignment) {
                StmtAssignment assignment = (StmtAssignment) statement;

                // To be fixed
                Variable lValue = ((LValueVariable) assignment.getLValue()).getVariable();

                VarDecl originalDecl = null;//declarations.declaration(lValue); // TODO: change
                Variable newNumberedVar = Variable.variable(originalDecl.getName() + "_" + getVariableNumber(lValue));
                incrementVariableNumber(lValue);
                writeVariable(lValue, new ExprVariable(newNumberedVar));
                LocalVarDecl numberedVarDecl = new LocalVarDecl(originalDecl.getAnnotations(), originalDecl.getType(),
                        originalDecl.getName(), originalDecl.getValue(), originalDecl.isConstant());
                addDecl(numberedVarDecl);

                StmtAssignment updatedStmt;
                if (assignment.getExpression() instanceof ExprVariable) {
                    Variable rValue = ((ExprVariable) assignment.getExpression()).getVariable();
                    Expression expr = readVariable(rValue);
                    updatedStmt = new StmtAssignment(new LValueVariable(newNumberedVar), expr);
                } else {
                    updatedStmt = new StmtAssignment(new LValueVariable(newNumberedVar), assignment.getExpression());
                }
                it.set(updatedStmt);
            }

            else if (statement instanceof StmtBlock) {
                // We need to split the SSABlocks in order to retain correctness with respect to block predecessors
                LinkedList<Statement> iteratedStmts = new LinkedList<>(stmtsIter.subList(0, it.previousIndex()));

                StmtBlock stmtBlock = (StmtBlock) statement;
                SSABlock innerBlockEntry = new SSABlock(stmtBlock.getTypeDecls(), stmtBlock.getVarDecls());
                innerBlockEntry.addPredecessor(this);
                innerBlockEntry.seal();
                SSABlock innerBlockExit = innerBlockEntry.fill(stmtBlock.getStatements());

                List<Statement> nextStmts = new ArrayList<>(stmtsIter.subList(it.nextIndex(), stmtsIter.size()));
                SSABlock nextBlock = new SSABlock();
                nextBlock.addPredecessor(innerBlockExit);
                nextBlock.seal();

                iteratedStmts.add(innerBlockEntry);
                iteratedStmts.add(nextBlock);
                setStatements(iteratedStmts); // We finished filling that block
                return nextBlock.fill(nextStmts);
            }

            else if (statement instanceof StmtIf) {
                List<Statement> iteratedStmts = new ArrayList<>(stmtsIter.subList(0, it.previousIndex()));

                StmtIf stmtIf = (StmtIf) statement;
                SSABlock thenEntry = new SSABlock();
                thenEntry.addPredecessor(this);
                thenEntry.seal();
                SSABlock thenExit = thenEntry.fill(stmtIf.getThenBranch());
                SSABlock elseEntry = new SSABlock();
                elseEntry.addPredecessor(this);
                elseEntry.seal();
                SSABlock elseExit = elseEntry.fill(stmtIf.getElseBranch());

                List<Statement> nextStmts = new ArrayList<>(stmtsIter.subList(it.nextIndex(), stmtsIter.size()));
                SSABlock nextBlock = new SSABlock();
                nextBlock.addPredecessor(thenExit);
                nextBlock.addPredecessor(elseExit);
                nextBlock.seal();

                Expression newCond = null; // TODO: replace stmtIf.getCondition() using readVariable
                StmtIf updatedStmt = new StmtIf(newCond, Arrays.asList(thenEntry), Arrays.asList(elseEntry));
                iteratedStmts.add(updatedStmt);
                iteratedStmts.add(nextBlock);
                setStatements(iteratedStmts);
                return nextBlock.fill(nextStmts);
            }
        }

        setStatements(stmtsIter);
        return this;
    }

    public StmtBlock getStmtBlock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return null;
    }
}

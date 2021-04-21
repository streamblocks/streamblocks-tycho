package se.lth.cs.tycho.transformation.ssa;

import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.AbstractDecl;
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
import se.lth.cs.tycho.ir.stmt.ssa.StmtPhi;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SSABlock extends Statement {

    private List<TypeDecl> typeDecls;
    private LinkedList<LocalVarDecl> varDecls;
    private final List<SSABlock> predecessors = new LinkedList<>();
    private final Map<String, Expression> currentDef = new HashMap<>();
    private final Map<String, Integer> currentNumber = new HashMap<>();
    private List<Statement> statements;
    private LinkedList<Phi> phis = new LinkedList<>();
    private boolean sealed = false;
    private final SSABlock programEntry;
    private final VariableDeclarations declarations;

    private SSABlock(SSABlock original, List<TypeDecl> typeDecls, LinkedList<LocalVarDecl> varDecls,
                     SSABlock programEntry, VariableDeclarations declarations) {
        super(original);
        this.typeDecls = typeDecls;
        this.varDecls = varDecls;
        this.programEntry = programEntry;
        this.declarations = declarations;
        for (LocalVarDecl decl: varDecls) {
            Variable originalVar = Variable.variable(decl.getName());
            writeVariable(originalVar, new ExprVariable(originalVar));
            programEntry.currentNumber.put(originalVar.getName(), 1);
        }
    }

    public SSABlock(List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls, SSABlock programEntry, VariableDeclarations declarations) {
        this(null, typeDecls, new LinkedList<>(varDecls), programEntry, declarations);
    }

    public SSABlock(SSABlock programEntry, VariableDeclarations declarations) {
        this(new LinkedList<>(), new LinkedList<>(), programEntry, declarations);
    }

    private SSABlock(VariableDeclarations declarations, SSABlock original) {
        super(original);
        this.programEntry = this;
        this.declarations = declarations;
        this.typeDecls = new LinkedList<>();
        this.varDecls = new LinkedList<>();
    }

    public SSABlock(VariableDeclarations declarations) {
        this(declarations, null);
    }

    public void seal() {
        phis.forEach(Phi::addOperands);
        sealed = true;
    }

    public int getVariableNumber(Variable variable) {
        /*if (currentNumber.containsKey(variable.getName())) {
            return currentNumber.get(variable.getName());
        }
        return predecessors.get(0).getVariableNumber(variable);*/
        return programEntry.currentNumber.get(variable.getName());
    }

    public void incrementVariableNumber(Variable variable) {
        //currentNumber.put(variable.getName(), getVariableNumber(variable) + 1);
        programEntry.currentNumber.put(variable.getName(), getVariableNumber(variable) + 1);
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
        currentDef.put(variable.getName(), value);
    }

    public Expression readVariable(Variable variable) {
        if (currentDef.containsKey(variable.getName())) {
            return currentDef.get(variable.getName()).deepClone();
        }
        return readVariableRecursive(variable);
    }

    public Expression readVariableRecursive(Variable variable) {
        if (predecessors.size() == 1) {
            return predecessors.get(0).readVariable(variable);
        }
        Expression res;
        VarDecl originalDecl = (VarDecl) declarations.declaration(variable).deepClone();
        Variable newNumberedVar = Variable.variable(originalDecl.getName() + "_" + getVariableNumber(variable));
        incrementVariableNumber(variable);
        LocalVarDecl numberedVarDecl = new LocalVarDecl(originalDecl.getAnnotations(), originalDecl.getType(),
                newNumberedVar.getName(), null, originalDecl.isConstant());
        programEntry.addDecl(numberedVarDecl);
        /*if (!sealed) {
            phis.add(new Phi(newNumberedVar, false));
            res = new ExprVariable(newNumberedVar);
        } else if (predecessors.size() == 1) {
            res = predecessors.get(0).readVariable(variable);
        } else {
            phis.add(new Phi(newNumberedVar, true));
            res = new ExprVariable(newNumberedVar);
        }
        writeVariable(variable, res);*/
        Phi phi = new Phi(newNumberedVar, variable);
        phis.add(phi);
        if (sealed) {
            phi.addOperands();
        }
        res = new ExprVariable(newNumberedVar);
        writeVariable(variable, res);
        return res;

        //return predecessors.get(0).readVariable(variable);
    }

    /*public void addPhiOperands(Phi phi) {

    }*/

    // programEntry argument is temporary, a recursive solution is probably better (and will deal with shadowing problem)
    public SSABlock fill(List<Statement> statements) {
        ReplaceVariablesInExpression replacer = MultiJ.from(ReplaceVariablesInExpression.class).instance();

        //int splitIndex = 0;
        LinkedList<Statement> stmtsIter = new LinkedList<>(statements);
        ListIterator<Statement> it = stmtsIter.listIterator();
        while(it.hasNext()) {
            Statement statement = it.next();

            if (statement instanceof StmtAssignment) {
                StmtAssignment assignment = (StmtAssignment) statement;

                // To be fixed
                Variable lValue = ((LValueVariable) assignment.getLValue()).getVariable();

                Expression newExpr = replacer.replaceVariables(assignment.getExpression(), this);
                //VarDecl originalDecl = declarations.declaration(lValue);
                VarDecl originalDecl = (VarDecl) declarations.declaration(lValue).deepClone();
                Variable newNumberedVar = Variable.variable(originalDecl.getName() + "_" + getVariableNumber(lValue));
                incrementVariableNumber(lValue);
                writeVariable(lValue, new ExprVariable(newNumberedVar));
                LocalVarDecl numberedVarDecl = new LocalVarDecl(originalDecl.getAnnotations(), originalDecl.getType(),
                        newNumberedVar.getName(), null, originalDecl.isConstant());
                programEntry.addDecl(numberedVarDecl);

                StmtAssignment updatedStmt;
                /*if (assignment.getExpression() instanceof ExprVariable) {
                    Variable rValue = ((ExprVariable) assignment.getExpression()).getVariable();
                    Expression expr = readVariable(rValue);
                    updatedStmt = new StmtAssignment(new LValueVariable(newNumberedVar), expr);
                } else {
                    updatedStmt = new StmtAssignment(new LValueVariable(newNumberedVar), assignment.getExpression());
                }*/
                updatedStmt = new StmtAssignment(new LValueVariable(newNumberedVar), newExpr);
                it.set(updatedStmt);
            }

            else if (statement instanceof StmtBlock) {
                // We need to split the SSABlocks in order to retain correctness with respect to block predecessors
                LinkedList<Statement> iteratedStmts = new LinkedList<>(stmtsIter.subList(0, it.previousIndex()));

                StmtBlock stmtBlock = (StmtBlock) statement;
                SSABlock innerBlockEntry = new SSABlock(stmtBlock.getTypeDecls(), stmtBlock.getVarDecls(), programEntry, declarations);
                innerBlockEntry.addPredecessor(this);
                innerBlockEntry.seal();
                SSABlock innerBlockExit = innerBlockEntry.fill(stmtBlock.getStatements());

                if (it.nextIndex() >= stmtsIter.size()) {
                    iteratedStmts.add(innerBlockEntry);
                    setStatements(iteratedStmts);
                    return innerBlockExit;
                }

                List<Statement> nextStmts = new ArrayList<>(stmtsIter.subList(it.nextIndex(), stmtsIter.size()));
                SSABlock nextBlock = new SSABlock(programEntry, declarations);
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
                SSABlock thenEntry = new SSABlock(programEntry, declarations);
                thenEntry.addPredecessor(this);
                thenEntry.seal();
                SSABlock thenExit = thenEntry.fill(stmtIf.getThenBranch());
                SSABlock elseEntry = new SSABlock(programEntry, declarations);
                elseEntry.addPredecessor(this);
                elseEntry.seal();
                SSABlock elseExit = elseEntry.fill(stmtIf.getElseBranch());

                List<Statement> nextStmts = new ArrayList<>(stmtsIter.subList(it.nextIndex(), stmtsIter.size()));
                SSABlock nextBlock = new SSABlock(programEntry, declarations);
                nextBlock.addPredecessor(thenExit);
                nextBlock.addPredecessor(elseExit);
                nextBlock.seal();

                Expression newCond = replacer.replaceVariables(stmtIf.getCondition(), this); // TODO: replace stmtIf.getCondition() using readVariable
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

        List<Statement> phiStmts = phis.stream()
                .map(p -> new StmtPhi(new LValueVariable(p.assigned), p.operands)).collect(Collectors.toList());
        List<Statement> completed = statements.stream().map(statement -> {
            if (statement instanceof SSABlock) return ((SSABlock) statement).getStmtBlock();
            if (statement instanceof StmtIf) {
                StmtIf stmtIf = (StmtIf) statement;
                StmtBlock thenn = ((SSABlock) (stmtIf.getThenBranch().get(0))).getStmtBlock();
                StmtBlock elze = ((SSABlock) (stmtIf.getElseBranch().get(0))).getStmtBlock();
                return new StmtIf(stmtIf.getCondition(), Arrays.asList(thenn), Arrays.asList(elze));
            }
            else return statement;
        }).collect(Collectors.toList());
        List<Statement> statements = Stream.concat(phiStmts.stream(), completed.stream()).collect(Collectors.toList());

        return new StmtBlock(typeDecls, varDecls, statements);
    }

    /*public List<Statement> getStmts() {

        List<Statement> phiStmts = phis.stream()
                .map(p -> new StmtPhi(new LValueVariable(p.assigned), p.operands)).collect(Collectors.toList());
        List<Statement> completed = statements.stream().map(statement -> {
            if (statement instanceof SSABlock) return ((SSABlock) statement).getStmts();
            if (statement instanceof StmtIf) {
                StmtIf stmtIf = (StmtIf) statement;
                List<Statement> thenn = ((SSABlock) (stmtIf.getThenBranch().get(0))).getStmts();
                List<Statement> elze = ((SSABlock) (stmtIf.getElseBranch().get(0))).getStmts();
                Statement newIf = new StmtIf(stmtIf.getCondition(), thenn, elze);
                return Arrays.asList(newIf);
            }
            else return Arrays.asList(statement);
        }).flatMap(Collection::stream).collect(Collectors.toList());
        List<Statement> statements = Stream.concat(phiStmts.stream(), completed.stream()).collect(Collectors.toList());

        return statements;
    }*/

    public List<TypeDecl> getTypeDecls() { return typeDecls; }

    public List<LocalVarDecl> getVarDecls() { return varDecls; }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return null;
    }

    private class Phi {
        private final Variable assigned;
        private final Variable target;
        private LinkedList<Expression> operands;

        public Phi(Variable assigned, Variable target) {
            this.assigned = assigned;
            this.target = target;
        }

        public void addOperands() {
            operands = new LinkedList<>(predecessors.stream().map(p -> p.readVariable(target)).collect(Collectors.toList()));
        }

        public Statement getStatement() {
            if (operands.size() == 1) {
                return new StmtAssignment(new LValueVariable(assigned), operands.get(0));
            }
            return new StmtPhi(new LValueVariable(assigned), operands);
        }
    }

}



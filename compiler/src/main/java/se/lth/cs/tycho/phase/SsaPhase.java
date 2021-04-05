package se.lth.cs.tycho.phase;

import jdk.vm.ci.meta.Local;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.ssa.*;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lth.cs.tycho.ir.Variable.variable;

/**
 * The Ssa phase.
 */
public class SsaPhase implements Phase {

    private static VariableDeclarations declarations = null;
    private final Map<Variable, Map<StmtBlock, Expression>> currentDef = new HashMap<>();

    /**
     * Instantiates a new Ssa phase.
     *
     * @param cfgOnly only build cfg
     */
    public SsaPhase(boolean cfgOnly) {
    }

    @Override
    public String getDescription() {
        return "Creates a CFG and optionally applies SSA transformation";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        declarations = task.getModule(VariableDeclarations.key);
        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class).instance();
        return task.transformChildren(transformation);
    }


//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    @Module
    interface Transformation extends IRNode.Transformation {

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        /**
         * Creates a CFG and applies SSA to an ExprProcReturn
         *
         * @param proc the ExprProcReturn
         * @return the updated ExprProcReturn
         */
        default IRNode apply(ExprProcReturn proc) {
            throw new UnsupportedOperationException();
        }

        default IRNode apply(Transition transition) {

            StmtBlock body = (StmtBlock) transition.getBody().get(0);
            List<Statement> statements;
            if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
                StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
                statements = ImmutableList.of(startingBlock);
            } else {
                statements = body.getStatements();
            }
            statements.forEach(stmt -> System.out.println(stmt.toString()));


            //return transition.withBody(ImmutableList.of(entryLabeled.getSsaStmt()));
            return transition.withBody(statements);
        }

    }

    /*public StmtBlock fillBlock(StmtBlock block) {

        return null;
    }*/

    public List<Statement> fillBlock(List<Statement> statements, SSABlock block) {

        int splitIndex = 0;
        for (Statement statement: statements) {
            if (statement instanceof StmtAssignment) {
                StmtAssignment assignment = (StmtAssignment) statement;

                // To be fixed
                Variable lValue = ((LValueVariable) assignment.getLValue()).getVariable();
                LocalVarDecl originalDecl = block.getOriginalDefinition(lValue);
                //String newNumberedVar = new Variable(originalDecl.getName() + "_" + block.getVariableNumber(lValue));

                if (assignment.getExpression() instanceof ExprVariable) {
                    Variable rValue = ((ExprVariable) assignment.getExpression()).getVariable();
                    Expression expr = readVariable(rValue, block);
                    writeVariable(lValue, block, expr);
                } else {
                    writeVariable(lValue, block, assignment.getExpression());
                }
            }

            splitIndex++;
        }

        return null;
    }


// ------------------------------------------------------ SSA algorithm core -------------------------------------------------------------

    /*public void writeVariable(Variable variable, StmtBlock block, Expression value) {
        currentDef.putIfAbsent(variable, new HashMap<>());
        Map<StmtBlock, Expression> currentDefVar = currentDef.get(variable);
        currentDefVar.put(block, value);
    }

    public Expression readVariable(Variable variable, StmtBlock block) {
        if (currentDef.get(variable).containsKey(block)) {
            currentDef.get(variable).get(block);
        }
        return readVariableRecursive(variable, block);
    }

    public Expression readVariableRecursive(Variable variable, StmtBlock block) {
        throw new UnsupportedOperationException();
    }*/

    public void writeVariable(Variable variable, SSABlock block, Expression value) {
        throw new UnsupportedOperationException();
    }

    public Expression readVariable(Variable variable, SSABlock block) {
        throw new UnsupportedOperationException();
    }

    public Expression readVariableRecursive(Variable variable, SSABlock block) {
        throw new UnsupportedOperationException();
    }

// ------------------------------------------------------ SSA data structure -------------------------------------------------------------

    private class SSABlock extends Statement {

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

        public void addDecl(LocalVarDecl decl) {
            varDecls.add(decl);
        }

        public void setStatements(List<Statement> statements) {
            this.statements = statements;
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

}
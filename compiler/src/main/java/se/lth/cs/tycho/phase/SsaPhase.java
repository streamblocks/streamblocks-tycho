package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
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
import se.lth.cs.tycho.transformation.ssa.SSABlock;
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

    private static VariableDeclarations declarations;
    //private final Map<Variable, Map<StmtBlock, Expression>> currentDef = new HashMap<>();
    //private static final CollectOrReplaceExpressions subExprCollectorOrReplacer = MultiJ.from(CollectOrReplaceExpressions.class).instance();

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

            //SSABlock programEntry = new SSABlock(body.getTypeDecls(), body.getVarDecls());
            //SSABlock exit = programEntry.fill(body.getStatements(), declarations);
            //return transition.withBody(Arrays.asList(programEntry.getStmtBlock()));
            SSABlock programEntry = new SSABlock();
            SSABlock exit = programEntry.fill(transition.getBody(), declarations);
            List<Statement> res = programEntry.getStmtBlock().getStatements();
            return transition.withBody(res);
            //return transition.withBody(programEntry.getStmtBlock().getStatements());
        }



    }




}
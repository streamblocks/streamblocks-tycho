package se.lth.cs.tycho.transformation.am2procedural;

import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.procedural.Procedural;
import se.lth.cs.tycho.ir.expr.ExprAvailInput;
import se.lth.cs.tycho.ir.expr.ExprAvailOutput;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprProcReturn;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtReturn;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;
import se.lth.cs.tycho.settings.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AmToProcedural {

    private final ActorMachine actor;
    private final ImmutableList<VarDecl> functions;
    private final ImmutableList<Scope> scopes;

    private final Configuration configuration;

    public AmToProcedural(ActorMachine actorMachine, Configuration configuration, Types types, TreeShadow tree) {
        this.actor = actorMachine;
        this.configuration = configuration;

        ProceduralFunctions proceduralFunctions = new ProceduralFunctions(actorMachine);
        ImmutableList.Builder<VarDecl> builder = ImmutableList.builder();
        builder.addAll(proceduralFunctions.createConditionDecls());
        builder.addAll(proceduralFunctions.createTransitionDecls());
        //builder.add(proceduralFunctions.createControllerDecl());
        this.functions = builder.build();

        this.scopes = ImmutableList.empty();
    }

    public Procedural buildProcedural() {
        return new Procedural(actor.getAnnotations(), actor.getInputPorts(), actor.getOutputPorts(), actor.getTypeParameters(), actor.getValueParameters(), actor.getScopes(), functions);
    }


    private class ProceduralFunctions {
        private final ActorMachine actorMachine;

        public ProceduralFunctions(ActorMachine actorMachine) {
            this.actorMachine = actorMachine;
        }

        /**
         * Transform Actor Machine conditions into Declarations
         *
         * @return
         */
        public List<VarDecl> createConditionDecls() {
            List<VarDecl> declarations = new ArrayList<>();

            for (Condition condition : actorMachine.getConditions()) {
                String name = "condition_" + actorMachine.getConditions().indexOf(condition);
                Expression expression;

                if (condition.kind() == Condition.ConditionKind.input) {
                    PortCondition port = (PortCondition) condition;

                    ExprAvailInput availInput = new ExprAvailInput(port.getPortName());
                    ExprLiteral literal = new ExprLiteral(ExprLiteral.Kind.Integer, String.valueOf(port.N()));

                    expression = new ExprBinaryOp(ImmutableList.of("<="), ImmutableList.of(availInput, literal));
                } else if (condition.kind() == Condition.ConditionKind.output) {
                    PortCondition port = (PortCondition) condition;

                    ExprAvailOutput spaceOutput = new ExprAvailOutput(port.getPortName());
                    ExprLiteral literal = new ExprLiteral(ExprLiteral.Kind.Integer, String.valueOf(port.N()));

                    expression = new ExprBinaryOp(ImmutableList.of("<="), ImmutableList.of(spaceOutput, literal));
                } else {
                    PredicateCondition predicate = (PredicateCondition) condition;
                    expression = predicate.getExpression().deepClone();
                }


                StmtReturn ret = new StmtReturn(expression);

                NominalTypeExpr boolType = new NominalTypeExpr("bool");

                ExprProcReturn proc = new ExprProcReturn(ImmutableList.empty(), ImmutableList.of(ret), boolType);

                VarDecl condDecl = new LocalVarDecl(Collections.emptyList(), boolType, name, proc, false);
                declarations.add(condDecl);
            }

            return declarations;
        }

        /**
         * Transform Actor Machine transitions into Declarations
         *
         * @return
         */
        public List<VarDecl> createTransitionDecls() {
            List<VarDecl> declarations = new ArrayList<>();

            for (Transition transition : actorMachine.getTransitions()) {
                String name = "transition_" + actorMachine.getConditions().indexOf(transition);
                ImmutableList.Builder<Statement> builder = ImmutableList.builder();

                builder.addAll(transition.getBody());

                ExprProcReturn proc = new ExprProcReturn(ImmutableList.empty(), builder.build(), null);
                LocalVarDecl decl = new LocalVarDecl(Collections.emptyList(), null, name, proc, true);

                declarations.add(decl);
            }

            return declarations;
        }

        /**
         * Transform Actor Machine controller into a Declaration
         *
         * @return
         */
        public VarDecl createControllerDecl() {
            VarDecl controller = null;


            return controller;
        }

    }


}

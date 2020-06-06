package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.*;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.StructureForeachStmt;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternTuple;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.stream.Stream;

public interface VariableScopes {
    ModuleKey<VariableScopes> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .instance();

    /**
     * Returns a list of variable declarations that are declared in scope rooted in the given node.
     *
     * @param node variable scope root
     * @return variable declarations
     */
    ImmutableList<VarDecl> declarations(IRNode node);

    @Module
    interface Implementation extends VariableScopes {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default ImmutableList<VarDecl> declarations(IRNode node) {
            return ImmutableList.empty();
        }

        // Expressions

        default ImmutableList<VarDecl> declarations(ExprLambda lambda) {
            return ImmutableList.covariance(lambda.getValueParameters());
        }

        default ImmutableList<VarDecl> declarations(ExprProc proc) {
            return ImmutableList.covariance(proc.getValueParameters());
        }

        default ImmutableList<VarDecl> declarations(ExprLet let) {
            return ImmutableList.covariance(let.getVarDecls());
        }

        default ImmutableList<VarDecl> declarations(ExprComprehension comp) {
            return ImmutableList.covariance(comp.getGenerator().getVarDecls());
        }

        // Statements

        default ImmutableList<VarDecl> declarations(StmtBlock block) {
            return ImmutableList.covariance(block.getVarDecls());
        }

        default ImmutableList<VarDecl> declarations(StmtForeach foreach) {
            return ImmutableList.covariance(foreach.getGenerator().getVarDecls());
        }

        // Cal

        default ImmutableList<VarDecl> declarations(CalActor actor) {
            return ImmutableList.concat(actor.getValueParameters(), actor.getVarDecls());
        }

        default ImmutableList<VarDecl> declarations(Action action) {
            Stream<VarDecl> inputVariables = action.getInputPatterns().stream()
                    .flatMap(input -> declarations(input).stream());
            Stream<LocalVarDecl> actionVariables = action.getVarDecls().stream();
            return Stream.concat(inputVariables, actionVariables)
                    .collect(ImmutableList.collector());
        }

        default ImmutableList<VarDecl> declarations(InputPattern input) {
            return input.getMatches().stream()
                    .flatMap(match -> declarations(match).stream())
                    .collect(ImmutableList.collector());
        }

        default ImmutableList<VarDecl> declarations(Match match) {
            return Stream
                    .concat(Stream.of(match.getDeclaration()), match.getExpression() == null ? Stream.<VarDecl>empty() : match.getExpression().getAlternatives().stream().flatMap(alternative -> declarations(alternative).stream()))
                    .collect(ImmutableList.collector());
        }

        // Actor Machine

        default ImmutableList<VarDecl> declarations(ActorMachine actorMachine) {
            Stream<LocalVarDecl> scopeVariables = actorMachine.getScopes().stream()
                    .map(Scope::getDeclarations)
                    .flatMap(ImmutableList::stream);
            Stream<ParameterVarDecl> parameters = actorMachine.getValueParameters().stream();
            return Stream.concat(scopeVariables, parameters)
                    .collect(ImmutableList.collector());
        }

        // Network

        default ImmutableList<VarDecl> declarations(NlNetwork net) {
            return ImmutableList.concat(net.getValueParameters(), net.getVarDecls());
        }

        default ImmutableList<VarDecl> declarations(StructureForeachStmt foreach) {
            return ImmutableList.covariance(foreach.getGenerator().getVarDecls());
        }

        // Namespace

        default ImmutableList<VarDecl> declarations(NamespaceDecl ns) {
            Stream<GlobalVarDecl> local = ns.getVarDecls().stream();

            CompilationTask task = (CompilationTask) tree().root();
            Stream<GlobalVarDecl> global = task.getSourceUnits().stream()
                    .map(SourceUnit::getTree)
                    .filter(decl -> decl.getQID().equals(ns.getQID()))
                    .map(NamespaceDecl::getVarDecls)
                    .flatMap(ImmutableList::stream);

            return Stream.concat(local, global)
                    .distinct()
                    .collect(ImmutableList.collector());
        }

        // Type declaration

        default ImmutableList<VarDecl> declarations(AlgebraicTypeDecl decl) {
            return decl.getValueParameters().map(VarDecl.class::cast);
        }

        // Case

        default ImmutableList<VarDecl> declarations(StmtCase.Alternative alternative) {
            return declarations(alternative.getPattern());
        }

        default ImmutableList<VarDecl> declarations(ExprCase.Alternative alternative) {
            return declarations(alternative.getPattern());
        }

        default ImmutableList<VarDecl> declarations(Pattern pattern) {
            return ImmutableList.empty();
        }

        default ImmutableList<VarDecl> declarations(PatternBinding pattern) {
            return ImmutableList.of(pattern.getDeclaration());
        }

        default ImmutableList<VarDecl> declarations(PatternDeconstruction pattern) {
            return pattern.getPatterns().stream().flatMap(p -> declarations(p).stream()).collect(ImmutableList.collector());
        }

        default ImmutableList<VarDecl> declarations(PatternList pattern) {
            return pattern.getPatterns().stream().flatMap(p -> declarations(p).stream()).collect(ImmutableList.collector());
        }

        default ImmutableList<VarDecl> declarations(PatternTuple pattern) {
            return pattern.getPatterns().stream().flatMap(p -> declarations(p).stream()).collect(ImmutableList.collector());
        }

        default ImmutableList<VarDecl> declarations(PatternAlias pattern) {
            return declarations(pattern.getAlias());
        }
    }
}

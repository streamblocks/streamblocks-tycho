package se.lth.cs.tycho.transformation.am2procedural;

import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.procedural.Procedural;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;
import se.lth.cs.tycho.settings.Configuration;

public class AmToProcedural {

    private final ActorMachine actor;
    private final ImmutableList<Scope> scopes;
    private final ImmutableList<VarDecl> functions;


    private final Configuration configuration;

    public AmToProcedural(ActorMachine actorMachine, Configuration configuration, Types types, TreeShadow tree) {
        this.actor = actorMachine;
        this.configuration = configuration;
        this.scopes = ImmutableList.empty();
        this.functions = ImmutableList.empty();
    }

    public Procedural buildProcedural() {
        return new Procedural(actor.getAnnotations(), actor.getInputPorts(), actor.getOutputPorts(), actor.getTypeParameters(), actor.getValueParameters(), actor.getScopes(), functions);
    }
    
}

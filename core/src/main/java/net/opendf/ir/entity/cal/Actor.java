/* 
BEGINCOPYRIGHT X,UC
	
	Copyright (c) 2007, Xilinx Inc.
	Copyright (c) 2003, The Regents of the University of California
	All rights reserved.
	
	Redistribution and use in source and binary forms, 
	with or without modification, are permitted provided 
	that the following conditions are met:
	- Redistributions of source code must retain the above 
	  copyright notice, this list of conditions and the 
	  following disclaimer.
	- Redistributions in binary form must reproduce the 
	  above copyright notice, this list of conditions and 
	  the following disclaimer in the documentation and/or 
	  other materials provided with the distribution.
	- Neither the names of the copyright holders nor the names 
	  of contributors may be used to endorse or promote 
	  products derived from this software without specific 
	  prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
	CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
	INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
	HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
	OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	
ENDCOPYRIGHT
 */

package net.opendf.ir.entity.cal;

import java.util.Objects;

import net.opendf.ir.QID;
import net.opendf.ir.decl.LocalTypeDecl;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclType;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.entity.EntityDefinition;
import net.opendf.ir.entity.EntityVisitor;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class Actor extends EntityDefinition {

	public Actor(ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Action> initializers, ImmutableList<Action> actions, ScheduleFSM scheduleFSM,
			ImmutableList<ImmutableList<QID>> priorities, ImmutableList<Expression> invariants) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, initializers,
				actions, scheduleFSM, priorities, invariants);
	}

	private Actor(Actor original, ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Action> initializers, ImmutableList<Action> actions, ScheduleFSM scheduleFSM,
			ImmutableList<ImmutableList<QID>> priorities, ImmutableList<Expression> invariants) {
		super(original, inputPorts, outputPorts, typePars, valuePars);

		this.typeDecls = ImmutableList.copyOf(typeDecls);
		this.varDecls = ImmutableList.copyOf(varDecls);
		this.initializers = ImmutableList.copyOf(initializers);
		this.actions = ImmutableList.copyOf(actions);
		this.scheduleFSM = scheduleFSM;
		this.priorities = ImmutableList.copyOf(priorities);
		this.invariants = ImmutableList.copyOf(invariants);
	}

	public Actor copy(ImmutableList<ParDeclType> typePars,
			ImmutableList<ParDeclValue> valuePars, ImmutableList<LocalTypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Action> initializers, ImmutableList<Action> actions, ScheduleFSM scheduleFSM,
			ImmutableList<ImmutableList<QID>> priorities, ImmutableList<Expression> invariants) {
		if (Lists.equals(getTypeParameters(), typePars) && Lists.equals(getValueParameters(), valuePars)
				&& Lists.equals(getTypeDecls(), typeDecls) && Lists.equals(getVarDecls(), varDecls)
				&& Lists.equals(getInputPorts(), inputPorts) && Lists.equals(getOutputPorts(), outputPorts)
				&& Lists.equals(this.initializers, initializers) && Lists.equals(this.actions, actions)
				&& Objects.equals(this.scheduleFSM, scheduleFSM) && Lists.equals(this.priorities, priorities)
				&& Lists.equals(this.invariants, invariants)) {
			return this;
		}
		return new Actor(this, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				initializers, actions, scheduleFSM, priorities, invariants);
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitActor(this, param);
	}

	public ImmutableList<LocalTypeDecl> getTypeDecls() {
		return typeDecls;
	}
	
	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
	}

	public ImmutableList<Action> getActions() {
		return actions;
	}

	public ImmutableList<Action> getInitializers() {
		return initializers;
	}

	public ImmutableList<Expression> getInvariants() {
		return invariants;
	}

	public ScheduleFSM getScheduleFSM() {
		return scheduleFSM;
	}

	public ImmutableList<ImmutableList<QID>> getPriorities() {
		return priorities;
	}

	private ImmutableList<LocalVarDecl> varDecls;
	private ImmutableList<LocalTypeDecl> typeDecls;
	private ImmutableList<Action> actions;
	private ScheduleFSM scheduleFSM;
	private ImmutableList<ImmutableList<QID>> priorities;
	private ImmutableList<Expression> invariants;

	private ImmutableList<Action> initializers;
}
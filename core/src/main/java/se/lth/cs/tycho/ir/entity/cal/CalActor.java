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

package se.lth.cs.tycho.ir.entity.cal;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class CalActor extends Entity {

	public CalActor(List<TypeDecl> typePars,
			List<ParameterVarDecl> valuePars, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls,
			List<PortDecl> inputPorts, List<PortDecl> outputPorts,
			List<Action> initializers, List<Action> actions, List<ActionCase> actionCases, ScheduleFSM scheduleFSM,
			ProcessDescription process, List<ImmutableList<QID>> priorities,
			List<Expression> invariants) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, initializers,
				actions, actionCases, scheduleFSM, process, priorities, invariants);
	}

	private CalActor(CalActor original, List<TypeDecl> typePars,
			List<ParameterVarDecl> valuePars, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls,
			List<PortDecl> inputPorts, List<PortDecl> outputPorts,
			List<Action> initializers, List<Action> actions, List<ActionCase> actionCases, ScheduleFSM scheduleFSM,
			ProcessDescription process,	List<ImmutableList<QID>> priorities,
			List<Expression> invariants) {
		super(original, inputPorts, outputPorts, typePars, valuePars);

		this.typeDecls = ImmutableList.from(typeDecls);
		this.varDecls = ImmutableList.from(varDecls);
		this.initializers = ImmutableList.from(initializers);
		this.actions = ImmutableList.from(actions);
		this.actionCases = ImmutableList.from(actionCases);
		this.scheduleFSM = scheduleFSM;
		this.process = process;
		this.priorities = ImmutableList.from(priorities);
		this.invariants = ImmutableList.from(invariants);
	}

	public CalActor copy(List<TypeDecl> typePars,
			List<ParameterVarDecl> valuePars, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls,
			List<PortDecl> inputPorts, List<PortDecl> outputPorts,
			List<Action> initializers, List<Action> actions, List<ActionCase> actionCases, ScheduleFSM scheduleFSM,
			ProcessDescription process, List<ImmutableList<QID>> priorities, List<Expression> invariants) {
		if (Lists.sameElements(this.typeParameters, typePars)
				&& Lists.sameElements(this.valueParameters, valuePars)
				&& Lists.sameElements(this.typeDecls, typeDecls)
				&& Lists.sameElements(this.varDecls, varDecls)
				&& Lists.sameElements(this.inputPorts, inputPorts)
				&& Lists.sameElements(this.outputPorts, outputPorts)
				&& Lists.sameElements(this.initializers, initializers)
				&& Lists.sameElements(this.actions, actions)
				&& Lists.sameElements(this.actionCases, actionCases)
				&& this.scheduleFSM == scheduleFSM
				&& this.process == process
				&& Lists.sameElements(this.priorities, priorities)
				&& Lists.sameElements(this.invariants, invariants)) {
			return this;
		}
		return new CalActor(this, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				initializers, actions, actionCases, scheduleFSM, process, priorities, invariants);
	}

	public ImmutableList<TypeDecl> getTypeDecls() {
		return typeDecls;
	}
	
	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
	}

	public ImmutableList<Action> getActions() {
		return actions;
	}

	public ImmutableList<ActionCase> getActionCases() {
		return actionCases;
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

	public ProcessDescription getProcessDescription() {
		return process;
	}

	public ImmutableList<ImmutableList<QID>> getPriorities() {
		return priorities;
	}

	private ImmutableList<LocalVarDecl> varDecls;
	private ImmutableList<TypeDecl> typeDecls;
	private ImmutableList<Action> actions;
	private ImmutableList<ActionCase> actionCases;
	private ScheduleFSM scheduleFSM;
	private ProcessDescription process;
	private ImmutableList<ImmutableList<QID>> priorities;
	private ImmutableList<Expression> invariants;

	private ImmutableList<Action> initializers;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		varDecls.forEach(action);
		typeDecls.forEach(action);
		initializers.forEach(action);
		actions.forEach(action);
		actionCases.forEach(action);
		if (scheduleFSM != null) action.accept(scheduleFSM);
		if (process != null) action.accept(process);
		invariants.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public CalActor transformChildren(Transformation transformation) {
		return copy(
				(ImmutableList) typeParameters.map(transformation),
				(ImmutableList) valueParameters.map(transformation),
				(ImmutableList) typeDecls.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) inputPorts.map(transformation),
				(ImmutableList) outputPorts.map(transformation),
				(ImmutableList) initializers.map(transformation),
				(ImmutableList) actions.map(transformation),
				(ImmutableList) actionCases.map(transformation),
				scheduleFSM == null ? null : (ScheduleFSM) transformation.apply(scheduleFSM),
				process == null ? null : (ProcessDescription) transformation.apply(process),
				priorities,
				(ImmutableList) invariants.map(transformation)
		);
	}

	public CalActor withVarDecls(List<LocalVarDecl> varDecls) {
		if (Lists.sameElements(this.varDecls, varDecls)) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, actions, actionCases, scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withValueParameters(ImmutableList<ParameterVarDecl> valueParameters) {
		if (Lists.sameElements(this.valueParameters, valueParameters)) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, actions, actionCases, scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withProcessDescription(ProcessDescription process) {
		if (process == this.process) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, actions, actionCases, scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withScheduleFSM(ScheduleFSM scheduleFSM) {
		if (this.scheduleFSM == scheduleFSM) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, actions, actionCases, scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withActions(List<Action> actions) {
		if (Lists.sameElements(this.actions, actions)) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, ImmutableList.from(actions), actionCases, scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withActionCases(List<ActionCase> actionCases) {
		if (Lists.sameElements(this.actionCases, actionCases)) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, actions, ImmutableList.from(actionCases), scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withInitialisers(List<Action> initializers) {
		if (Lists.sameElements(this.initializers, initializers)) {
			return this;
		} else {
 			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, ImmutableList.from(initializers), actions, actionCases, scheduleFSM, process, priorities, invariants);
		}
	}

	public CalActor withPriorities(List<ImmutableList<QID>> priorities) {
		if (Lists.sameElements(this.priorities, priorities)) {
			return this;
		} else {
			return new CalActor(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, initializers, actions, actionCases, scheduleFSM, process, ImmutableList.from(priorities), invariants);
		}
	}
}

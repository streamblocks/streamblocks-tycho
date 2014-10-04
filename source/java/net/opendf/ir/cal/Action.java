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

package net.opendf.ir.cal;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.QID;
import net.opendf.ir.common.decl.LocalTypeDecl;
import net.opendf.ir.common.decl.LocalVarDecl;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.common.stmt.Statement;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class Action extends AbstractIRNode {

	public Action(int id, QID tag, ImmutableList<InputPattern> inputPatterns,
			ImmutableList<OutputExpression> outputExpressions, ImmutableList<LocalTypeDecl> typeDecls,
			ImmutableList<LocalVarDecl> varDecls, ImmutableList<Expression> guards, ImmutableList<Statement> body,
			Expression delay, ImmutableList<Expression> preconditions, ImmutableList<Expression> postconditions) {
		this(null, id, tag, inputPatterns, outputExpressions, typeDecls, varDecls, guards, body, delay, preconditions,
				postconditions);
	}

	private Action(Action original, int id, QID tag, ImmutableList<InputPattern> inputPatterns,
			ImmutableList<OutputExpression> outputExpressions, ImmutableList<LocalTypeDecl> typeDecls,
			ImmutableList<LocalVarDecl> varDecls, ImmutableList<Expression> guards, ImmutableList<Statement> body,
			Expression delay, ImmutableList<Expression> preconditions, ImmutableList<Expression> postconditions) {
		super(original);
		this.id = id;
		this.tag = tag;
		this.inputPatterns = ImmutableList.copyOf(inputPatterns);
		this.outputExpressions = ImmutableList.copyOf(outputExpressions);
		this.typeDecls = ImmutableList.copyOf(typeDecls);
		this.varDecls = ImmutableList.copyOf(varDecls);
		this.guards = ImmutableList.copyOf(guards);
		this.body = ImmutableList.copyOf(body);
		this.delay = delay;
		this.preconditions = ImmutableList.copyOf(preconditions);
		this.postconditions = ImmutableList.copyOf(postconditions);
	}

	public Action copy(int id, QID tag, ImmutableList<InputPattern> inputPatterns,
			ImmutableList<OutputExpression> outputExpressions, ImmutableList<LocalTypeDecl> typeDecls,
			ImmutableList<LocalVarDecl> varDecls, ImmutableList<Expression> guards, ImmutableList<Statement> body,
			Expression delay, ImmutableList<Expression> preconditions, ImmutableList<Expression> postconditions) {
		if (this.id == id && Objects.equals(this.tag, tag) && Lists.equals(this.inputPatterns, inputPatterns)
				&& Lists.equals(this.outputExpressions, outputExpressions) && Lists.equals(this.typeDecls, typeDecls)
				&& Lists.equals(this.varDecls, varDecls) && Lists.equals(this.guards, guards)
				&& Lists.equals(this.body, body) && Objects.equals(this.delay, delay)
				&& Lists.equals(this.preconditions, preconditions) && Lists.equals(this.postconditions, postconditions)) {
			return this;
		}
		return new Action(this, id, tag, inputPatterns, outputExpressions, typeDecls, varDecls, guards, body, delay,
				preconditions, postconditions);
	}

	public QID getTag() {
		return tag;
	}

	public ImmutableList<InputPattern> getInputPatterns() {
		return inputPatterns;
	}

	public ImmutableList<OutputExpression> getOutputExpressions() {
		return outputExpressions;
	}

	public ImmutableList<LocalTypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
	}

	public ImmutableList<Expression> getGuards() {
		return guards;
	}

	public ImmutableList<Statement> getBody() {
		return body;
	}

	public Expression getDelay() {
		return delay;
	}

	public int getID() {
		return id;
	}

	public ImmutableList<Expression> getPreconditions() {
		return preconditions;
	}

	public ImmutableList<Expression> getPostconditions() {
		return postconditions;
	}

	private int id;
	private QID tag;
	private ImmutableList<InputPattern> inputPatterns;
	private ImmutableList<OutputExpression> outputExpressions;
	private ImmutableList<LocalTypeDecl> typeDecls;
	private ImmutableList<LocalVarDecl> varDecls;
	private ImmutableList<Expression> guards;
	private ImmutableList<Statement> body;
	private Expression delay;
	private ImmutableList<Expression> preconditions;
	private ImmutableList<Expression> postconditions;

	public String toString() {
		return "(Action " + tag + ")";
		// Utility.increaseTabDepth(2);
		// String tabs = Utility.getHeadingTabs();
		// String result = "Action " + this.tag + ":\n" + tabs +
		// "inputPatterns:\n" +
		// Utility.arrayToString(this.inputPatterns) + "\n" + tabs +
		// "outputExpressions:\n" +
		// Utility.arrayToString(this.outputExpressions);
		// Utility.decreaseTabDepth(2);
		// return result;
	}
}

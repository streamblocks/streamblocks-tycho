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

import java.util.Objects;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class InputPattern extends AbstractIRNode {

	public InputPattern(Port port, ImmutableList<VarDecl> variables, Expression repeatExpr) {
		this(null, port, variables, repeatExpr);
	}

	private InputPattern(InputPattern original, Port port, ImmutableList<VarDecl> variables, Expression repeatExpr) {
		super(original);
		this.port = port;
		this.variables = ImmutableList.copyOf(variables);
		this.repeatExpr = repeatExpr;
	}

	public InputPattern copy(Port port, ImmutableList<VarDecl> variables, Expression repeatExpr) {
		if (Objects.equals(this.port, port) && Lists.equals(this.variables, variables)
				&& Objects.equals(this.repeatExpr, repeatExpr)) {
			return this;
		}
		return new InputPattern(this, port, variables, repeatExpr);
	}

	public Port getPort() {
		return port;
	}

	public ImmutableList<VarDecl> getVariables() {
		return variables;
	}

	public Expression getRepeatExpr() {
		return repeatExpr;
	}

	private Port port;
	private ImmutableList<VarDecl> variables;
	private Expression repeatExpr;

}

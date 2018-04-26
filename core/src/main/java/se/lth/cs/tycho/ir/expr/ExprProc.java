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

package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExprProc extends Expression {

	public ExprProc(List<ParameterVarDecl> valueParams, List<Statement> body) {
		this(null, valueParams, body);
	}

	public ExprProc(List<ParameterVarDecl> valueParams) {
		this(null, valueParams, null);
	}

	private ExprProc(ExprProc original, List<ParameterVarDecl> valueParams, List<Statement> body) {
		super(original);
		this.valueParameters = ImmutableList.from(valueParams);
		this.body = ImmutableList.from(body);
	}
	
	private ExprProc copy(List<ParameterVarDecl> valueParams, List<Statement> body) {
		if (Lists.sameElements(valueParameters, valueParams)
				&& Lists.sameElements(this.body, body)) {
			return this;
		}
		return new ExprProc(this, valueParams, body);
	}

	public ImmutableList<ParameterVarDecl> getValueParameters() {
		return valueParameters;
	}

	public ExprProc withValueParameters(List<ParameterVarDecl> valueParameters) {
		return copy(valueParameters, body);
	}

	public ImmutableList<Statement> getBody() {
		return body;
	}

	public ExprProc withBody(List<Statement> body) {
		return copy(valueParameters, body);
	}

	private final ImmutableList<ParameterVarDecl> valueParameters;
	private final ImmutableList<Statement> body;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		valueParameters.forEach(action);
		body.forEach(action);
	}

	@Override
	public ExprProc transformChildren(Transformation transformation) {
		return copy(
				transformation.mapChecked(ParameterVarDecl.class, valueParameters),
				transformation.mapChecked(Statement.class, body)
		);
	}
}

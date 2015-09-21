/* 
BEGINCOPYRIGHT X
	
	Copyright (c) 2007, Xilinx Inc.
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
	- Neither the name of the copyright holder nor the names 
	  of its contributors may be used to endorse or promote 
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

package se.lth.cs.tycho.ir;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class GeneratorFilter extends AbstractIRNode {

	public ImmutableList<VarDecl> getVariables() {
		return variables;
	}

	public Expression getCollectionExpr() {
		return collectionExpr;
	}

	public ImmutableList<Expression> getFilters() {
		return filters;
	}

	public GeneratorFilter(ImmutableList<VarDecl> variables, Expression collectionExpr,
			ImmutableList<Expression> filters) {
		this(null, variables, collectionExpr, filters);
	}

	private GeneratorFilter(GeneratorFilter original, ImmutableList<VarDecl> variables, Expression collectionExpr,
			ImmutableList<Expression> filters) {
		super(original);
		this.variables = ImmutableList.copyOf(variables);
		this.collectionExpr = collectionExpr;
		this.filters = ImmutableList.copyOf(filters);
	}

	public GeneratorFilter copy(ImmutableList<VarDecl> variables, Expression collectionExpr,
			ImmutableList<Expression> filters) {
		if (Lists.equals(this.variables, variables) && Objects.equals(this.collectionExpr, collectionExpr)
				&& Lists.equals(this.filters, filters)) {
			return this;
		}
		return new GeneratorFilter(this, variables, collectionExpr, filters);
	}

	private ImmutableList<VarDecl> variables;
	private Expression collectionExpr;
	private ImmutableList<Expression> filters;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(collectionExpr);
		variables.forEach(action);
		filters.forEach(action);
	}

	@Override
	public GeneratorFilter transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) variables.map(transformation),
				(Expression) transformation.apply(collectionExpr),
				(ImmutableList) filters.map(transformation)
		);
	}
}

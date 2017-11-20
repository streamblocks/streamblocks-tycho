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
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExprLambda extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprLambda(this, p);
	}

	public ExprLambda(List<ParameterVarDecl> valueParams, Expression body, TypeExpr returnTypeExpr) {
		this(null, valueParams, body, returnTypeExpr);
	}
	public ExprLambda(List<ParameterVarDecl> valueParams, TypeExpr returnTypeExpr) {
		this(null, valueParams, null, returnTypeExpr);
	}
	private ExprLambda(ExprLambda original, List<ParameterVarDecl> valueParams, Expression body, TypeExpr returnTypeExpr) {
		super(original);
		this.valueParameters = ImmutableList.from(valueParams);
		this.body = body;
		this.returnTypeExpr = returnTypeExpr;
	}

	public ExprLambda copy(List<ParameterVarDecl> valueParams,
						   Expression body, TypeExpr returnTypeExpr) {
		if (Lists.sameElements(valueParameters, valueParams) && this.body == body && this.returnTypeExpr == returnTypeExpr) {
			return this;
		}
		return new ExprLambda(this, valueParams, body, returnTypeExpr);
	}

	public ImmutableList<ParameterVarDecl> getValueParameters() {
		return valueParameters;
	}

	public Expression getBody() {
		return body;
	}

	public TypeExpr getReturnType() {
		return returnTypeExpr;
	}

	private final ImmutableList<ParameterVarDecl> valueParameters;
	private final Expression body;
	private final TypeExpr returnTypeExpr;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		valueParameters.forEach(action);
		if (returnTypeExpr != null) action.accept(returnTypeExpr);
		if (body != null) action.accept(body);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ExprLambda transformChildren(Transformation transformation) {
		return copy(
				transformation.mapChecked(ParameterVarDecl.class, valueParameters),
				body == null ? null : transformation.applyChecked(Expression.class, body),
				returnTypeExpr == null ? null : transformation.applyChecked(TypeExpr.class, returnTypeExpr));
	}
}

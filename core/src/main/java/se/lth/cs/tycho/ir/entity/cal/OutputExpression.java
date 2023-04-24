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
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class OutputExpression extends AbstractIRNode {

	public OutputExpression(Port port, ImmutableList<Expression> values, Expression repeatExpr, Expression arrayIndexExpression) {
		this(null, port, values, repeatExpr, arrayIndexExpression);
	}

	public OutputExpression(Port port, ImmutableList<Expression> values, Expression repeatExpr) {
		this(null, port, values, repeatExpr, null);
	}

	public OutputExpression(OutputExpression original, Port port, ImmutableList<Expression> values,
			Expression repeatExpr, Expression arrayIndexExpression) {
		super(original);
		this.port = port;
		this.values = ImmutableList.from(values);
		this.repeatExpr = repeatExpr;
		this.arrayIndexExpression = arrayIndexExpression;
	}

	public OutputExpression copy(Port port, ImmutableList<Expression> values, Expression repeatExpr, Expression arrayIndexExpression) {
		if (Objects.equals(this.port, port) && Lists.equals(this.values, values)
				&& Objects.equals(this.repeatExpr, repeatExpr) && Objects.equals(this.arrayIndexExpression, arrayIndexExpression)) {
			return this;
		}
		return new OutputExpression(this, port, values, repeatExpr, arrayIndexExpression);
	}

	public Port getPort() {
		return port;
	}

	public ImmutableList<Expression> getExpressions() {
		return values;
	}

	public Expression getRepeatExpr() {
		return repeatExpr;
	}

	/**
	 * @author Gareth Callanan
	 *
	 * Returns the expression representing the index within the port array that this port refers to.
	 *
	 * This is an output expression that has a firing action linked to an array port.
	 * For example, in "action X:[x] ==> Z[0]:[x+1] end", Z[0]:[x+1] is an OutputExpression.
	 * Where Z[...] is the corresponding array port.
	 *
	 * If null, then this is not an array port.
	 */
	public Expression getArrayIndexExpression(){
		return arrayIndexExpression;
	}

	private Port port;
	private ImmutableList<Expression> values;
	private Expression repeatExpr;

	// If this port actually represents an array of ports, then this expression
	// indicates the index within the port array that this OutputExpression refers to.
	// Null otherwise
	private Expression arrayIndexExpression;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
		values.forEach(action);
		if (repeatExpr != null) action.accept(repeatExpr);
		if (arrayIndexExpression != null) action.accept(arrayIndexExpression);
	}

	@Override
	@SuppressWarnings("unchecked")
	public OutputExpression transformChildren(Transformation transformation) {
		return copy(
				(Port) transformation.apply(port),
				(ImmutableList) values.map(transformation),
				repeatExpr == null ? null : (Expression) transformation.apply(repeatExpr),
				arrayIndexExpression == null ? null : (Expression) transformation.apply(arrayIndexExpression)
		);
	}

	/**
	 *  Replaces the port object within the OutputExpression.
	 *
	 *	@param newPort The newPort that will replace this objects current port.
	 *
	 *  @return The same OutputExpression object with the port replaced.
	 */
	public OutputExpression withPort(Port newPort){
		return new OutputExpression(this, newPort, values, repeatExpr, arrayIndexExpression);
	}

	/**
	 *  Replaces the port object within the OutputExpression and sets the arrayIndexExpression to null.
	 *
	 *	@param newPort The newPort that will replace this objects current port.
	 *
	 *  @return The same OutputExpression object with the port replaced.
	 */
	public OutputExpression withPortNoIndexExpression(Port newPort){
		return new OutputExpression(this, newPort, values, repeatExpr, null);
	}

	/**
	 * Apply a transformation to the arrayIndexExpression and repeatExpression only.
	 *
	 * @param transformation Transformation to apply.
	 * @return Input pattern with the transformed port array index and repeat expressions.
	 */
	public OutputExpression transformPortArrayAndRepeat(Transformation transformation) {
		return new OutputExpression(this, port, values, repeatExpr == null ? null :
				(Expression) transformation.apply(repeatExpr), arrayIndexExpression == null ? null :
				(Expression) transformation.apply(arrayIndexExpression));
	}
}

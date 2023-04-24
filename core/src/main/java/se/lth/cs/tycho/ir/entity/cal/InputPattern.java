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

public class InputPattern extends AbstractIRNode {

	private Port port;
	private ImmutableList<Match> matches;
	private Expression repeatExpr;

	// If this port represents an array of ports, then this expression
	// indicates the index within the port array that this InputPattern refers to.
	// Null otherwise
	private Expression arrayIndexExpression;

	public InputPattern(Port port, ImmutableList<Match> matches, Expression repeatExpr, Expression arrayIndexExpression) {
		this(null, port, matches, repeatExpr, arrayIndexExpression);
	}

	public InputPattern(Port port, ImmutableList<Match> matches, Expression repeatExpr) {
		this(null, port, matches, repeatExpr, null);
	}

	protected InputPattern(InputPattern original, Port port, ImmutableList<Match> matches, Expression repeatExpr, Expression arrayIndexExpression) {
		super(original);
		this.port = port;
		this.matches = ImmutableList.from(matches);
		this.repeatExpr = repeatExpr;
		this.arrayIndexExpression = arrayIndexExpression;
	}

	public InputPattern copy(Port port, ImmutableList<Match> matches, Expression repeatExpr , Expression arrayIndexExpression) {
		if (Objects.equals(this.getPort(), port) && Lists.equals(this.getMatches(), matches)
				&& Objects.equals(this.getRepeatExpr(), repeatExpr)  && Objects.equals(this.arrayIndexExpression, arrayIndexExpression) ) {
			return this;
		}
		return new InputPattern(this, port, matches, repeatExpr, arrayIndexExpression);
	}

	public Port getPort() {
		return port;
	}

	public ImmutableList<Match> getMatches() {
		return matches;
	}

	public Expression getRepeatExpr() {
		return repeatExpr;
	}

	/**
	 * @author Gareth Callanan
	 *
	 * Returns the expression representing the index within the port array that this InputPattern object to.
	 *
	 * This is an InputPattern that has a firing action linked to an array port.
	 * For example, in "action X[0]:[x] ==> Z:[x+1] end", X[0]:[x] is an array port input pattern.
	 * Where X[...] is the corresponding array port.
	 *
	 * If null, then this is not an array port.
	 */
	public Expression getArrayIndexExpression(){
		return arrayIndexExpression;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
		matches.forEach(action);
		if (repeatExpr != null) action.accept(repeatExpr);
		if (arrayIndexExpression != null) action.accept(arrayIndexExpression);
	}

	@Override
	@SuppressWarnings("unchecked")
	public InputPattern transformChildren(Transformation transformation) {
		return copy(
				(Port) transformation.apply(port),
				(ImmutableList) matches.map(transformation),
				repeatExpr == null ? null : (Expression) transformation.apply(repeatExpr),
				arrayIndexExpression == null ? null : (Expression) transformation.apply(arrayIndexExpression)
		);
	}

	/**
	 *  Replaces the port object within the InputPattern.
	 *
	 *	@param newPort The newPort that will replace this objects current port.
	 *
	 *  @return The same InputPattern object with the port replaced.
	 */
	public InputPattern withPort(Port newPort){
		return new InputPattern(this, newPort, matches, repeatExpr, arrayIndexExpression);
	}

	/**
	 *  Replaces the port object within the InputPattern and sets the arrayIndex expression to null.
	 *
	 *	@param newPort The newPort that will replace this objects current port.
	 *
	 *  @return The same InputPattern object with the port replaced.
	 */
	public InputPattern withPortNoIndexExpression(Port newPort){
		return new InputPattern(this, newPort, matches, repeatExpr, null);
	}

	/**
	 * Apply a transformation to the arrayIndexExpression and repeatExpression only.
	 *
	 * @param transformation Transformation to apply.
	 * @return Input pattern with the transformed port array index and repeat expression.
	 */
	public InputPattern transformPortArrayAndRepeat(Transformation transformation) {
		return new InputPattern(this, port, matches, repeatExpr == null ? null :
				(Expression) transformation.apply(repeatExpr), arrayIndexExpression == null ? null :
				(Expression) transformation.apply(arrayIndexExpression));
	}
}

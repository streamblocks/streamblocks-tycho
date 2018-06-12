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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * An abstract syntax tree node that represents a function application expression.
 */
public class ExprApplication extends Expression {
	private Expression function;
	private ImmutableList<Expression> args;

	/**
	 * Constructs an instance with the given function node {@code function}
	 * and list of argument nodes {@code args}.
	 *
	 * @param function The function to apply 
	 * @param args The arguments to apply the function on
	 */
	public ExprApplication(Expression function, List<Expression> args) {
		this(null, function, args);
	}
	
	/**
	 * Constructs an instance as a new version of another node {@code
	 * original} with the given function node {@code function} and list of
	 * argument nodes {@code args}.  The parameter {@code original} should
	 * be null if the node does not have a precursor.
	 *
	 * @param original The precursor to the new node
	 * @param function The function to apply 
	 * @param args The arguments to apply the function on
	 */
	private ExprApplication(IRNode original, Expression function, List<Expression> args) {
		super(original);
		this.function = function;
		this.args = ImmutableList.from(args);
	}
	
	/**
	 * Returns a new version of the current node unless the given children
	 * (i.e. the function and the argument nodes) are the same as the
	 * current ones.  If the given children are the same as the current
	 * children, the current instance is returned.
	 * 
	 * @param function The function to apply
	 * @param args The arguments to apply the function on
	 * @return a function application node with the given arguments as children.
	 */
	public ExprApplication copy(Expression function, List<Expression> args) {
		if (Objects.equals(this.function, function) && Lists.equals(this.args, args)) {
			return this;
		}
		return new ExprApplication(this, function, args);
	}

	/**
	 * Returns the function to apply.
	 * @return the function to apply
	 */
	public Expression getFunction() {
		return function;
	}

	/**
	 * Returns a list with the arguments to apply the function on.
	 * @return the arguments to the function
	 */
	public ImmutableList<Expression> getArgs() {
		return args;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(function);
		args.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ExprApplication transformChildren(Transformation transformation) {
		return copy(
				(Expression) transformation.apply(function),
				(List) args.map(transformation)
		);
	}
}

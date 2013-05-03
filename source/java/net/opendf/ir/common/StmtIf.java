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

package net.opendf.ir.common;

import java.util.Objects;

/**
 * @author Jorn W. Janneck <janneck@eecs.berkeley.edu>
 */

public class StmtIf extends Statement {

	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtIf(this, p);
	}

	public StmtIf(Expression condition, Statement thenBranch, Statement elseBranch) {
		this(null, condition, thenBranch, elseBranch);
	}

	public StmtIf(Expression condition, Statement thenBranch) {
		this(condition, thenBranch, null);
	}

	private StmtIf(StmtIf original, Expression condition, Statement thenBranch, Statement elseBranch) {
		super(original);
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
	}

	public StmtIf copy(Expression condition, Statement thenBranch, Statement elseBranch) {
		if (Objects.equals(this.condition, condition) && Objects.equals(this.thenBranch, thenBranch)
				&& Objects.equals(this.elseBranch, elseBranch)) {
			return this;
		}
		return new StmtIf(this, condition, thenBranch, elseBranch);
	}

	public StmtIf copy(Expression condition, Statement thenBranch) {
		if (Objects.equals(this.condition, condition) && Objects.equals(this.thenBranch, thenBranch)
				&& this.elseBranch == null) {
			return this;
		}
		return new StmtIf(this, condition, thenBranch, null);
	}

	public Expression getCondition() {
		return condition;
	}

	public Statement getThenBranch() {
		return thenBranch;
	}

	public Statement getElseBranch() {
		return elseBranch;
	}

	private Expression condition;
	private Statement thenBranch;
	private Statement elseBranch;
}

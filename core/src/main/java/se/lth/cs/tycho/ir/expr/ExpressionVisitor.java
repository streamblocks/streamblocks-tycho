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

/**
 * @author Jorn W. Janneck <janneck@eecs.berkeley.edu>
 */

public interface ExpressionVisitor<R,P> {
	default R visitExpression(Expression e, P p) {
		return e.accept(this, p);
	}
    R visitExprApplication(ExprApplication e, P p);
    R visitExprBinaryOp(ExprBinaryOp e, P p);
    R visitExprField(ExprField e, P p);
    R visitExprIf(ExprIf e, P p);
    R visitExprIndexer(ExprIndexer e, P p);
    R visitExprInput(ExprInput e, P p);
    R visitExprLambda(ExprLambda e, P p);
    R visitExprLet(ExprLet e, P p);
    R visitExprList(ExprList e, P p);
    R visitExprLiteral(ExprLiteral e, P p);
    R visitExprMap(ExprMap e, P p);
    R visitExprMember(ExprMember e, P p);
    R visitExprProc(ExprProc e, P p);
    R visitExprSet(ExprSet e, P p);
    R visitExprUnaryOp(ExprUnaryOp e, P p);
    R visitExprVariable(ExprVariable e, P p);
}

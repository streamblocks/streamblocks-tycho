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

/**
 * @author Jorn W. Janneck <janneck@eecs.berkeley.edu>
 */

public interface ExpressionVisitor<R,P> {
    public R visitExprApplication(ExprApplication e, P p);
    public R visitExprBinaryOp(ExprBinaryOp e, P p);
    public R visitExprField(ExprField e, P p);
    public R visitExprIf(ExprIf e, P p);
    public R visitExprIndexer(ExprIndexer e, P p);
    public R visitExprInput(ExprInput e, P p);
    public R visitExprLambda(ExprLambda e, P p);
    public R visitExprLet(ExprLet e, P p);
    public R visitExprList(ExprList e, P p);
    public R visitExprLiteral(ExprLiteral e, P p);
    public R visitExprMap(ExprMap e, P p);
    public R visitExprProc(ExprProc e, P p);
    public R visitExprSet(ExprSet e, P p);
    public R visitExprUnaryOp(ExprUnaryOp e, P p);
    public R visitExprVariable(ExprVariable e, P p);
}

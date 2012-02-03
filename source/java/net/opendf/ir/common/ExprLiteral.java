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

public class ExprLiteral extends Expression {

    public void accept(ExpressionVisitor visitor) {
        visitor.visitExprLiteral(this);
    }
    
    public int getKind() {
		return kind;
	}
    
    public String getText() {
		return text;
	}

    public ExprLiteral(int kind) {
        this(kind, "");
    }

    /* FIXME: add some error checking here? */
    public ExprLiteral(int kind, String text) {
        this.kind = kind;
        this.text = text;
    }
    
    /**
     * Literal type.
     *
     * This will be any of the litXYZ constants defined below.
     */
    private int          kind;


    public final static int  litNull = 1;
    public final static int  litTrue = 2;
    public final static int  litFalse = 3;
    public final static int  litChar = 4;
    public final static int  litInteger = 5;
    public final static int  litReal = 6;
    public final static int  litString = 7;

    /**
     * Literal text (includes delimiters).
     *
     * This will be non-null only for litChar, litInteger, litFloat, and
     * litString literals.
     */
    private String       text;

    public String toString() {
        String text;
        switch(this.kind) {
            case litNull:
                text = "Null"; break;
            case litTrue:
                text = "True"; break;
            case litFalse:
                text = "False"; break;
            default:
                text = this.text;
        }
        return "Literal: " + text;
    }
}

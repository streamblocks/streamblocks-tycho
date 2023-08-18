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

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;

public class ExprLiteral extends Expression {

    public Kind getKind() {
        return kind;
    }

    public String getText() {
        return text;
    }

    public ExprLiteral(Kind kind) {
        this(null, kind, kind.getFixedText());
        assert kind.hasFixedText();
    }

    public ExprLiteral(Kind kind, String text) {
        this(null, kind, text);
    }

    /* FIXME: add some error checking here? */
    public ExprLiteral(IRNode original, Kind kind, String text) {
        super(original);
        this.kind = kind;
        this.text = text.intern();
    }

    public ExprLiteral copy(Kind kind, String text) {
        assert !kind.hasFixedText();
        if (this.kind == kind && Objects.equals(this.text, text)) {
            return this;
        }
        return new ExprLiteral(this, kind, text);
    }

    public ExprLiteral copy(Kind kind) {
        assert kind.hasFixedText();
        if (this.kind == kind) {
            return this;
        }
        return new ExprLiteral(this, kind, kind.getFixedText());
    }

    /**
     * Literal type.
     * <p>
     * This will be any of the litXYZ constants defined below.
     */
    private Kind kind;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }

    @Override
    public ExprLiteral transformChildren(Transformation transformation) {
        return this;
    }

    public enum Kind {
        Null("Null"), True("True"), False("False"), Char(null), Integer(null), Real(null), Complex_Real(null), Complex_Int(null) ,String(null), Function(null);

        private final String fixedText;

        private String getFixedText() {
            return fixedText;
        }

        private boolean hasFixedText() {
            return fixedText != null;
        }

        Kind(String fixedText) {
            this.fixedText = fixedText;
        }
    }

    public OptionalInt asInt() {
        if (kind != Kind.Integer) {
            return OptionalInt.empty();
        } else {
            String text = this.text;
            int radix = 10;
            if (text.startsWith("0x") || text.startsWith("0X")) {
                text = text.substring(2);
                radix = 16;
            } else if (text.startsWith("0b") || text.startsWith("0B")) {
                text = text.substring(2);
                radix = 2;
            } else if (text.startsWith("0") && text.length() > 1) {
                text = text.substring(1);
                radix = 8;
            }
            return OptionalInt.of(Integer.parseInt(text, radix));
        }
    }

    public OptionalInt intRadix() {
        if (kind != Kind.Integer) {
            return OptionalInt.empty();
        } else {
            int radix = 10;
            if (text.startsWith("0x") || text.startsWith("0X")) {
                radix = 16;
            } else if (text.startsWith("0b") || text.startsWith("0B")) {
                radix = 2;
            } else if (text.startsWith("0") && text.length() > 1) {
                radix = 8;
            }
            return OptionalInt.of(radix);
        }
    }

    public Optional<String> asString() {
        if (kind != Kind.String) {
            return Optional.empty();
        } else {
            return Optional.of(text.substring(1, text.length() - 1));
        }
    }

	public OptionalDouble asDouble() {
		if (kind != Kind.Real) {
			return OptionalDouble.empty();
		} else {
			return OptionalDouble.of(Double.parseDouble(text));
		}
	}

	/**
	 * Literal text (includes delimiters).
	 * 
	 * This will be non-null only for litChar, litInteger, litFloat, and
	 * litString literals.
	 */
	private String text;

    public String toString() {
        return "Literal: " + text;
    }
}


// this code will be inlined in the body of the generated scanner class
%{
  StringBuffer strbuf = new StringBuffer(128);
  int sub_line;
  int sub_column;
  int strlit_start_line, strlit_start_column;

  private Symbol sym(short id) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), str());
  }

  private Symbol sym(short id, String value) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), value);
  }

  private Symbol sym(short id, String value, int start_line, int start_column, int len) {
    return new Symbol(id, start_line, start_column, len, value);
  }

  private String str() { return yytext(); }
  private int len() { return yylength(); }

  private void error(String msg) throws Scanner.Exception {
    throw new Scanner.Exception(yyline + 1, yycolumn + 1, msg);
  }
%}

// 3.4 Line Terminators
LineTerminator = \n|\r|\r\n
InputCharacter = [^\r\n]

// 3.6 White Space
WhiteSpace = [ ] | \t | \f | {LineTerminator}

// 3.7 Comments
Comment = {TraditionalComment}
        | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/" | "/*" "*"+ [^/*] ~"*/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

// 3.8 Identifiers
/**
 * Ambiguity between IDENTIFIER and OPERATOR. $ can be part of both operators and identifiers
 * The scanner uses a greedy longest match approach to resolve this.
 * $ is parsed as OPERATOR($)
 * $id is parsed as IDENTIFIER($id)
 * $-id is parsed as an OPERATOR($-), IDENTIFIER(id)
 * id$name is parsed as IDENTIFIER(id$name)
 * id$-name is parsed as IDENTIFIER(id$), OPERATOR(-), IDENTIFIER(name)
 * id-$name is parsed as IDENTIFIER(id), OPERATOR(-$), IDENTIFIER(name)
 **/

TraditionalIdentifier = [:jletter:][:jletterdigit:]*  // any sequence of digits, letters, $ and _ is an identifier according to CLR, but $ is an potential operator. 
EscapedIdentifier = "\\" [^\\]* "\\"                  // \#-|*\ is parsed as IDENTIFIER(#-|*)

OperatorStart =[!@#$%\^&*/+\-Ñ<>?~\|]
Operator = {OperatorStart} ({OperatorStart}|[=])*  // "$" will be parsed as an OPERATOR. 
                                                       // the operator string may not start with | or = which are parsed as BAR and EQ
                                                       // "|" is needed in the FSM syntax: s1(a.b)-->s2 | (b)-->s0 | (c.c)-->s1; and in regular expressions
                                                       // "a=-b" should be parsed as "a = - b". Therefore an operator may not start with '='. (longest match will give the tokens "a", "=-", "b")
                                                       // ".." is parsed as an operator

// 3.10.1 Integer Literals
DecimalNumeral = 0 | {NonZeroDigit} {Digits}? 
HexNumeral = 0 [xX] [0-9a-fA-F]+
OctalNumeral = 0 [0-7]+

Digits = {Digit}+
Digit = 0 | {NonZeroDigit}
NonZeroDigit = [1-9]

// 3.10.2 Floating-Point Literals
FloatingPointLiteral = {Digits} \. {Digits}? {ExponentPart}?
                     | \. {Digits} {ExponentPart}?
                     | {Digits} {ExponentPart}
ExponentPart = [eE] [+-]? [0-9]+

// 3.10.4 Character Literals
SingleCharacter = [^\r\n\'\\]

// 3.10.5 String Literals
StringCharacter = [^\r\n\"\\]

// 3.10.6 Escape Sequences for Character and String Literals
OctalEscape = \\ {OctalDigit} 
            | \\ {OctalDigit} {OctalDigit}
            | \\  {ZeroToThree} {OctalDigit} {OctalDigit}
OctalDigit = [0-7]
ZeroToThree = [0-3]

%state STRING
%%

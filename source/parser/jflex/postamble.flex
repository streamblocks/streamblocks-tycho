
// 3.6 White Space
<YYINITIAL> { {WhiteSpace} { } }

// 3.7 Comments
<YYINITIAL> { {Comment} { } }

// 3.10 Literals
<YYINITIAL> {
  // 3.10.1 Integer Literals
  {DecimalNumeral} \.\.          { yypushback(2); return sym(Terminals.INTEGER_LITERAL); }                   // 1..4 => "1", "..", "4"  (without this rule it will be parsed as "1.", ".", "4")
  {DecimalNumeral}               { return sym(Terminals.INTEGER_LITERAL); }
  {DecimalNumeral} [lL]          { return sym(Terminals.LONG_LITERAL, str().substring(0,len()-1)); }

  {HexNumeral}                   { return sym(Terminals.INTEGER_LITERAL); }
  {HexNumeral} [lL]              { return sym(Terminals.LONG_LITERAL, str().substring(0, len()-1)); }

  {OctalNumeral}                 { return sym(Terminals.INTEGER_LITERAL); }
  {OctalNumeral} [lL]            { return sym(Terminals.LONG_LITERAL, str().substring(0, len()-1)); }

  // 3.10.2 Floating-Point Literals
  {DecimalNumeral} \.\.\.        { yypushback(2); return sym(Terminals.FLOATING_POINT_LITERAL); }                   // 1...4 => "1.", "..", "4"
  {FloatingPointLiteral} [fF]    { return sym(Terminals.FLOATING_POINT_LITERAL, str().substring(0,len()-1)); }
  {FloatingPointLiteral} [dD]    { return sym(Terminals.DOUBLE_LITERAL, str().substring(0,len()-1)); }
  {FloatingPointLiteral}         { return sym(Terminals.DOUBLE_LITERAL); }
  [0-9]+ {ExponentPart}? [fF]    { return sym(Terminals.FLOATING_POINT_LITERAL, str().substring(0,len()-1)); }
  [0-9]+ {ExponentPart}? [dD]    { return sym(Terminals.DOUBLE_LITERAL, str().substring(0,len()-1)); }
  
  // 3.10.3 Boolean Literals
  "true"                         { return sym(Terminals.TRUE_LITERAL); }
  "false"                        { return sym(Terminals.FALSE_LITERAL); }
  
  // 3.10.4 Character Literals
  \'{SingleCharacter}\'          { return sym(Terminals.CHARACTER_LITERAL, str().substring(1, len()-1)); }
  // 3.10.6 Escape Sequences for Character Literals
  \'"\\b"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\b"); }
  \'"\\t"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\t"); }
  \'"\\n"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\n"); }
  \'"\\f"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\f"); }
  \'"\\r"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\r"); }
  \'"\\\""\'                     { return sym(Terminals.CHARACTER_LITERAL, "\""); }
  \'"\\'"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\'"); }
  \'"\\\\"\'                     { return sym(Terminals.CHARACTER_LITERAL, "\\"); }
  \'{OctalEscape}\'              { int val = Integer.parseInt(str().substring(2,len()-1),8);
                                         return sym(Terminals.CHARACTER_LITERAL, new Character((char)val).toString()); }
  // Character Literal errors
  \'\\.                          { error("illegal escape sequence \""+str()+"\""); }
  \'{LineTerminator}             { error("unterminated character literal at end of line"); }

  // 3.10.5 String Literals
  \"                             { yybegin(STRING); 
                                   // remember start position of string literal so we can
                                   // set its position correctly in the end
                                   strlit_start_line = yyline+1;
                                   strlit_start_column = yycolumn+1;
                                   strbuf.setLength(0); 
                                 }

  // 3.10.7 The Null Literal
  "null"                         { return sym(Terminals.NULL_LITERAL); }
}

// 3.10.5 String Literals
<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return sym(Terminals.STRING_LITERAL, strbuf.toString(), strlit_start_line, strlit_start_column, strbuf.length()+2); }

  {StringCharacter}+             { strbuf.append(str()); }

  // 3.10.6 Escape sequences for String Literals
  "\\b"                          { strbuf.append( '\b' ); }
  "\\t"                          { strbuf.append( '\t' ); }
  "\\n"                          { strbuf.append( '\n' ); }
  "\\f"                          { strbuf.append( '\f' ); }
  "\\r"                          { strbuf.append( '\r' ); }
  "\\\""                         { strbuf.append( '\"' ); }
  "\\'"                          { strbuf.append( '\'' ); }
  "\\\\"                         { strbuf.append( '\\' ); }
  {OctalEscape}                  { strbuf.append((char)Integer.parseInt(str().substring(1),8)); }

  // String Literal errors
  \\.                            { error("illegal escape sequence \""+str()+"\""); }
  {LineTerminator}               { error("unterminated string at end of line"); }
}



// 3.11 Separators
<YYINITIAL> {
  ":"                            {return sym(Terminals.COLON);}
  "("                            { return sym(Terminals.LPAREN); }
  ")"                            { return sym(Terminals.RPAREN); }
  "{"                            { return sym(Terminals.LBRACE); }
  "}"                            { return sym(Terminals.RBRACE); }
  "["                            { return sym(Terminals.LBRACK); }
  "]"                            { return sym(Terminals.RBRACK); }
  ";"                            { return sym(Terminals.SEMICOLON); }
  ","                            { return sym(Terminals.COMMA); }
  "."                            { return sym(Terminals.DOT); }
  ".."                           { return sym(Terminals.OPERATOR); }
}

// Other symbols
<YYINITIAL> {
  "-->"                          { return sym(Terminals.EDGE);}
  "->"                           { return sym(Terminals.ARROW);}
  "==>"                          { return sym(Terminals.PORTCONN);}
  "::"                           { return sym(Terminals.DOUBLECOLON);}
  "="                            { return sym(Terminals.EQ); }
}

// Operators
// in addition to Terminals.OPERATOR the tokens Terminals.EQ and Terminals.IN can also be operators
<YYINITIAL> {
  ">"                           { return sym(Terminals.GT); }  // needed in priorities "action_a > action_b"
  "*"                           { return sym(Terminals.MULT); }  // needed in regular schedule expressions

  // unary operations
  "not"                          { return sym(Terminals.OPERATOR); }
  "dom"                          { return sym(Terminals.OPERATOR); }
  "rng"                          { return sym(Terminals.OPERATOR); }

  // binary operations
  "and"                          { return sym(Terminals.OPERATOR); }
  "or"                           { return sym(Terminals.OPERATOR); }
  "div"                          { return sym(Terminals.OPERATOR); }
  "mod"                          { return sym(Terminals.OPERATOR); }
  "in"                           { return sym(Terminals.IN); }  // needed in for generators "for i in 1..5"
  "bit_xor"                      { return sym(Terminals.OPERATOR); }
  "bit_or"                       { return sym(Terminals.OPERATOR); }
  {Operator}                     { // Some legal operator names are used in other parts of the language. Detect them.
                                   if(str().startsWith("->")){          // used by maps, "a->-b" is "a", "->", "-", "b"
                                     yypushback(str().length()-2);
                                     return sym(Terminals.ARROW, "->");
                                   }
                                   if("|".equals(str())){
                                     return sym(Terminals.BAR);
                                   }
                                   return sym(Terminals.OPERATOR); 
                                 }
//TODO, MULT is needed by regular expression schedules  
  // assignment
  ":="                           { return sym(Terminals.COLONEQ);}

  // regexp
//  | parsed as an expression "|"                            { return sym(Terminals.BAR); }
}

// 3.8 Identifiers located at end of current state due to rule priority disambiguation
<YYINITIAL> {
  {TraditionalIdentifier}       { return sym(Terminals.IDENTIFIER); }
  {EscapedIdentifier}           { return  sym(Terminals.IDENTIFIER, str().substring(1,len()-1)); }
}

// fall through errors
.|\n                            { error("illegal character \""+str()+ "\""); }
<<EOF>>                         {return sym(Terminals.EOF);}
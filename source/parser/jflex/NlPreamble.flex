package net.opendf.parser.lth; // The generated parser will belong to package AST_CAL

import beaver.Symbol;
import beaver.Scanner;
import net.opendf.parser.lth.NlParser.Terminals; // The terminals are implicitly defined in the parser
import java.io.*;
%%
// define the signature for the generated scanner
%public
%final
%class NlScanner 
%extends beaver.Scanner

// the interface between the scanner and the parser is the nextToken() method
%type Symbol 
%function nextToken 
%yylexthrow Scanner.Exception
// store line and column information in the tokens
%line
%column

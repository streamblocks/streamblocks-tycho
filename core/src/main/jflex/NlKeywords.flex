//---------------------------------------------------------------------

// 3.9 Keywords
<YYINITIAL> {
    // Keywords
    "network"                    {return sym(Terminals.NETWORK);}
    "entities"                   {return sym(Terminals.ENTITIES);}
    "structure"                  {return sym(Terminals.STRUCTURE);}

// common with the CAL scanner
    "entity"                     {return sym(Terminals.ENTITY);}
//    "type"                       {return sym(Terminals.TYPE);}  type can not be a reserved word for List(type:int, size=8) to parse

    "all"                        {return sym(Terminals.ALL);}
    "begin"                      {return sym(Terminals.BEGIN);}
    "choose"                     {return sym(Terminals.CHOOSE);}
    "const"                      {return sym(Terminals.CONST);}
    "do"                         {return sym(Terminals.DO);}
    "else"                       {return sym(Terminals.ELSE);}
    "end"                        {return sym(Terminals.END);}
    "endchoose"                  {return sym(Terminals.ENDCHOOSE);}
    "endforeach"                 {return sym(Terminals.ENDFOREACH);}
    //"endfunction"              {return sym(Terminals.ENDFUNCTION);}
    "endif"                      {return sym(Terminals.ENDIF);}
    //"endinitialize"            {return sym(Terminals.ENDINITIALIZE);}
    "endlambda"                  {return sym(Terminals.ENDLAMBDA);}
    "endlet"                     {return sym(Terminals.ENDLET);}
    //"endpriority"              {return sym(Terminals.ENDPRIORITY);}
    "endproc"                    {return sym(Terminals.ENDPROC);}
    //"endprocedure"             {return sym(Terminals.ENDPROCEDURE);}
    "endwhile"                   {return sym(Terminals.ENDWHILE);}
    "foreach"                    {return sym(Terminals.FOREACH);}
    "for"                        {return sym(Terminals.FOR);}
    "function"                   {return sym(Terminals.FUNCTION);}
    "if"                         {return sym(Terminals.IF);}
    "import"                     {return sym(Terminals.IMPORT);}
    "in"                         {return sym(Terminals.IN);}
    "lambda"                     {return sym(Terminals.LAMBDA);}
    "let"                        {return sym(Terminals.LET);}
    "map"                        {return sym(Terminals.MAP);}
    "mutable"                    {return sym(Terminals.MUTABLE);}
    "old"                        {return sym(Terminals.OLD);}
    "proc"                       {return sym(Terminals.PROC);}
    "procedure"                  {return sym(Terminals.PROCEDURE);}
    "then"                       {return sym(Terminals.THEN);}
    "var"                        {return sym(Terminals.VAR);}
    "while"                      {return sym(Terminals.WHILE);}

}

//---------------------------------------------------------------------

//---------------------------------------------------------------------

// 3.9 Keywords
<YYINITIAL> {
    // Keywords
    "action"                     {return sym(Terminals.ACTION);}
    "actor"                      {return sym(Terminals.ACTOR);}
    "all"                        {return sym(Terminals.ALL);}
    "any"                        {return sym(Terminals.ANY);}
    "at"                         {return sym(Terminals.AT);}
    "at*"                        {return sym(Terminals.ATSTAR);}
    "begin"                      {return sym(Terminals.BEGIN);}
    "choose"                     {return sym(Terminals.CHOOSE);}
    "const"                      {return sym(Terminals.CONST);}
    "delay"                      {return sym(Terminals.DELAY);}
    "do"                         {return sym(Terminals.DO);}
    "else"                       {return sym(Terminals.ELSE);}
    "end"                        {return sym(Terminals.END);}
    "endaction"                  {return sym(Terminals.ENDACTION);}
    "endactor"                   {return sym(Terminals.ENDACTOR);}
    "endchoose"                  {return sym(Terminals.ENDCHOOSE);}
    "endforeach"                 {return sym(Terminals.ENDFOREACH);}
    //"endfunction"              {return sym(Terminals.ENDFUNCTION);}
    "endif"                      {return sym(Terminals.ENDIF);}
    //"endinitialize"            {return sym(Terminals.ENDINITIALIZE);}
    "endinvariant"               {return sym(Terminals.ENDINVARIANT);}
    "endlambda"                  {return sym(Terminals.ENDLAMBDA);}
    "endlet"                     {return sym(Terminals.ENDLET);}
    //"endpriority"              {return sym(Terminals.ENDPRIORITY);}
    "endproc"                    {return sym(Terminals.ENDPROC);}
    //"endprocedure"             {return sym(Terminals.ENDPROCEDURE);}
    "endschedule"                {return sym(Terminals.ENDSCHEDULE);}
    "endwhile"                   {return sym(Terminals.ENDWHILE);}
    "foreach"                    {return sym(Terminals.FOREACH);}
    "for"                        {return sym(Terminals.FOR);}
    "fsm"                        {return sym(Terminals.FSM);}
    "function"                   {return sym(Terminals.FUNCTION);}
    "guard"                      {return sym(Terminals.GUARD);}
    "if"                         {return sym(Terminals.IF);}
    "import"                     {return sym(Terminals.IMPORT);}
    "initialize"                 {return sym(Terminals.INITIALIZE);}
    "invariant"                  {return sym(Terminals.INVARIANT);}
    "lambda"                     {return sym(Terminals.LAMBDA);}
    "let"                        {return sym(Terminals.LET);}
    "map"                        {return sym(Terminals.MAP);}
    "multi"                      {return sym(Terminals.MULTI);}
    "mutable"                    {return sym(Terminals.MUTABLE);}
    "old"                        {return sym(Terminals.OLD);}
    "priority"                   {return sym(Terminals.PRIORITY);}
    "proc"                       {return sym(Terminals.PROC);}
    "procedure"                  {return sym(Terminals.PROCEDURE);}
    "regexp"                     {return sym(Terminals.REGEXP);}
    "repeat"                     {return sym(Terminals.REPEAT);}
    "schedule"                   {return sym(Terminals.SCHEDULE);}
    "then"                       {return sym(Terminals.THEN);}
    "time"                       {return sym(Terminals.TIME);}
    "var"                        {return sym(Terminals.VAR);}
    "while"                      {return sym(Terminals.WHILE);}

    // Keywords "reserved for future use"
/*
    "assign"                     {return sym(Terminals.ASSIGN);}
    "case"                       {return sym(Terminals.CASE);}
    "default"                    {return sym(Terminals.DEFAULT);}
    "endtask"                    {return sym(Terminals.ENDTASK);}
    "endtype"                    {return sym(Terminals.ENDTYPE);}
    "ensure"                     {return sym(Terminals.ENSURE);}
    "now"                        {return sym(Terminals.NOW);}
    "out"                        {return sym(Terminals.OUT);}
    "protocol"                   {return sym(Terminals.PROTOCOL);}
    "require"                    {return sym(Terminals.REQUIRE);}
    "task"                       {return sym(Terminals.TASK);}
    "type"                       {return sym(Terminals.TYPE);}
*/
}

//---------------------------------------------------------------------

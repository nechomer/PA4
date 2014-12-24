package ic.lexer;
%%
%class Scanner
%type Token
%unicode
%line
%column
%{
  StringBuffer string = new StringBuffer();
  
  // store line, column info when entering a new state
  private int stateLine, stateCol;

  private void savePos() {
     stateLine = yyline+1;
     stateCol = yycolumn+1;  
  }

  // if restorePos is true, then use the previously saved line and column numbers.
  private Token token(String tag, String value, boolean restorePos) {
     return new Token(restorePos ? stateLine : (yyline+1),
                      restorePos ? stateCol : (yycolumn+1), tag, value); 
  }
  
  private void Error(String msg, boolean restorePos) {
      throw new LexerException(restorePos ? stateLine : (yyline+1), 
                               restorePos ? stateCol : (yycolumn+1), msg);
  }

%}

/****************************************** MACROS ****************************************/
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t]

EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}

IdentifierCharacter = [a-zA-Z0-9_]
ClassIdentifier = [A-Z]{IdentifierCharacter}*
RegularIdentifier = [a-z]{IdentifierCharacter}*

DecIntegerLiteral = (0 | [1-9][0-9]*)

Keyword = ("class" | "extends" | "static" | "void" | "int" | "boolean" | "string" |
           "return" | "if" | "else" | "while" | "break" | "continue" | "this" |
           "new" | "length" | "true" | "false" | "null")

Operator = ("[" | "]" | "(" | ")" | "." | "-" | "!" | "*" | "/" | "%" | "+" |
            "<" | "<=" | ">" | ">=" | "==" | "!=" | "&&" | "||" | "=")

// StringCharacter: allowed ASCII codes between decimal 32 and 126 excluding " and \.
// and the escape sequences: \", \\, \t, \n
StringCharacter = ([\040-\041\043-\133\135-\176] | "\\\"" | "\\\\" | "\\t" | "\\n")

Structure = [{};,]

%state STRING, TRADITIONAL_COMMENT

%%
/****************************************** RULES ****************************************/
<YYINITIAL> {
/* keywords */
{Keyword}                      { return token(yytext(), yytext(), false); }

/* identifiers */
"_" {IdentifierCharacter}*     { Error("an identifier cannot start with '_'", false);}
{RegularIdentifier}            { return token("ID", yytext(), false); }
{ClassIdentifier}              { return token("CLASS_ID", yytext(), false); }

/* literals */
0+ {DecIntegerLiteral}         { Error("numbers should not have leading zeros", false); }
{DecIntegerLiteral}            { return token("INTEGER", yytext(), false); }
\"                             { savePos(); string.setLength(0); string.append("\""); yybegin(STRING); }

/* operators */                                                 
{Operator}                     { return token(yytext(), yytext(), false); }
                                                                
/* structure */  
{Structure}                    { return token(yytext(), yytext(), false); }
                                                                
/* comments */                                 
{EndOfLineComment}             { /* ignore */ }         
"/*"                           { savePos(); yybegin(TRADITIONAL_COMMENT); }     
                                                                
/* whitespace */                                                
{WhiteSpace}                   { /* ignore */ }
}

<STRING> {
\"                             { yybegin(YYINITIAL); string.append("\""); return token("STRING", string.toString(), true); }
{StringCharacter}+             { string.append(yytext()); }
.|\n                           { Error("malformed string literal", true); }
<<EOF>>                        { Error("malformed string literal", true); }
}

<TRADITIONAL_COMMENT> {
[^\*]                          { /* ignore */ }
"*/"                           { yybegin(YYINITIAL); }
"*"                            { /* ignore */ }
<<EOF>>                        { Error("unterminated comment", true); }
}

/* error fallback */
.|\n                           { Error("invalid character '"+ yytext()+"'", false); }
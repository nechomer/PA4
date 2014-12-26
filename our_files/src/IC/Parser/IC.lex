package IC.Parser;
import IC.Parser.LexicalError;
import IC.Parser.sym;
/*********** Definitions ***********/
%%
%cup
%class Lexer
%public
%yylexthrow LexicalError
%type Token
%unicode
%line
%column
%{
	StringBuffer string = new StringBuffer();

	// save the last line and column of state
	private int lLine, lCol;

	private int getCurrentLine() {
		return yyline+1;
	}

	private int getCurrentColumn() {
		return yycolumn+1;
	}

	private void lastPos() {
		lLine = getCurrentLine();
		lCol = getCurrentColumn();  
	}

	// if flag == true => then use lastPos else currentPos
	private Token token(int id, String tag, Object value, boolean flag) {
		if(flag)
			return new Token(id,lLine,lCol,tag,value);
		else
			return new Token(id,getCurrentLine(),getCurrentColumn(),tag,value);
	}

	private Token token(int id, String tag, boolean flag) {
		if(flag)
			return new Token(id,lLine,lCol,tag);
		else
			return new Token(id,getCurrentLine(),getCurrentColumn(),tag);
	}

	private void Error(String token, boolean flag) throws LexicalError {
		if(flag)
			throw new LexicalError(lLine,lCol,token);
		else
			throw new LexicalError(getCurrentLine(),getCurrentColumn(),token);
	}

%}

/******** Macros *********/

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t]

LineComment     = "//" {InputCharacter}* {LineTerminator}

IdentifierCharacter = [a-zA-Z0-9_]
ClassIdentifier = [A-Z]{IdentifierCharacter}*
RegularIdentifier = [a-z]{IdentifierCharacter}*

DecIntegerLiteral = ([0-9]+)

//**************************** keywords ***********************************
classKeyword = "class"
extendsKeyword = "extends"
staticKeyword = "static" 
voidKeyword = "void"
intKeyword = "int"
booleanKeyword = "boolean"
stringKeyword = "string"
returnKeyword = "return"
ifKeyword = "if"
elseKeyword = "else"
whileKeyword = "while"
breakKeyword = "break"
continueKeyword = "continue"
thisKeyword = "this"
newKeyword = "new"
lengthKeyword = "length"
trueKeyword = "true"
falseKeyword = "false"
nullKeyword = "null"


//**************************** Operators ***********************************
leftBracketOperator = "["
rightBracketOperator = "]"
leftparenOperator = "("
rightparenOperator = ")"
dotOperator = "."
minusOperator = "-"
notOperator = "!"
multOperator = "*"
divOperator = "/"
moduluOperator = "%"
plusOperator = "+"
ltOperator = "<"
lteqOperator = "<="
gtOperator = ">"
gteqOperator = ">="
eqeqOperator = "=="
neqOperator = "!="
andandOperator = "&&"
ororOperator = "||"
eqOperator = "="

// StringCharacter:  ASCII codes incl 32 - 126 + " and \ + escape sequence: \", \\, \t, \n
StringCharacter = ([\040-\041\043-\133\135-\176])

IntegerOutOfRange=

        [1-9][0-9]{10}[0-9]*|

        [3-9][0-9]{9}[0-9]*|

        2[2-9][0-9]{8}[0-9]*|

        21[5-9][0-9]{7}[0-9]*|

        214[8-9][0-9]{6}[0-9]*|

        2147[5-9][0-9]{5}[0-9]*|

        21474[9-9][0-9]{4}[0-9]*|

        214748[4-9][0-9]{3}[0-9]*|

        2147483[7-9][0-9]{2}[0-9]*|

        21474836[5-9][0-9]{1}[0-9]*|

        214748364[8-9][0-9]*

//**************************** Stracture ***********************************
leftBracesStracture = "{"
rightBracesStracture = "}"
semiStracture = ";"
comaStracture = ","


%state STRING, TRADITIONAL_COMMENT

%%

/********* Rules and Actions **********/

<YYINITIAL> {

/* keywords */

{classKeyword}					{ return token(sym.CLASS, yytext(), false); }
{extendsKeyword}				{ return token(sym.EXTENDS, yytext(), false); }
{staticKeyword}					{ return token(sym.STATIC, yytext(), false); } 
{voidKeyword}					{ return token(sym.VOID, yytext(), false); }
{intKeyword}					{ return token(sym.INTEGER, yytext(), false); }
{booleanKeyword}				{ return token(sym.BOOLEAN, yytext(), false); }
{stringKeyword}					{ return token(sym.STRING, yytext(), false); }
{returnKeyword}					{ return token(sym.RETURN, yytext(), false); }
{ifKeyword}						{ return token(sym.IF, yytext(), false); }
{elseKeyword}					{ return token(sym.ELSE, yytext(), false); }
{whileKeyword}					{ return token(sym.WHILE, yytext(), false); }
{breakKeyword}					{ return token(sym.BREAK, yytext(), false); }
{continueKeyword}				{ return token(sym.CONTINUE, yytext(), false); }
{thisKeyword}					{ return token(sym.THIS, yytext(), false); }
{newKeyword}					{ return token(sym.NEW, yytext(), false); }
{lengthKeyword}					{ return token(sym.LENGTH, yytext(), false); }
{trueKeyword}					{ return token(sym.TRUE_LITERAL, yytext(), Boolean.valueOf(yytext()), false); }
{falseKeyword}					{ return token(sym.FALSE_LITERAL, yytext(), Boolean.valueOf(yytext()), false); }
{nullKeyword}					{ return token(sym.NULL_LITERAL, yytext(), false); }


/* identifiers */

"_" {IdentifierCharacter}*     { Error(yytext(), false);}
{RegularIdentifier}            { return token(sym.IDENTIFIER, "Identifier", yytext(), false); }
{ClassIdentifier}              { return token(sym.CLASS_ID, "CLASS_ID", yytext(), false); }


/* literals */

/* leading zero check, have been eliminated 0+ {DecIntegerLiteral}         { Error(yytext(), false); } */

{IntegerOutOfRange}			   { Error("Integer out Of Range: " + yytext(), false); }
{DecIntegerLiteral}            { return token(sym.INTEGER_LITERAL, "INTEGER", new Integer(yytext()), false); }
\"                             { lastPos(); string.setLength(0); yybegin(STRING); }


/* operators */

{leftBracketOperator}			{ return token(sym.LBRACKET, yytext(), false); }
{rightBracketOperator}			{ return token(sym.RBRACKET, yytext(), false); }
{leftparenOperator}				{ return token(sym.LPAREN, yytext(), false); }
{rightparenOperator}			{ return token(sym.RPAREN, yytext(), false); }
{dotOperator}					{ return token(sym.DOT, yytext(), false); }
{minusOperator}					{ return token(sym.MINUS, yytext(), false); }
{notOperator}					{ return token(sym.NOT, yytext(), false); }
{multOperator}					{ return token(sym.MULT, yytext(), false); }
{divOperator}					{ return token(sym.DIV, yytext(), false); }
{moduluOperator}				{ return token(sym.MODULU, yytext(), false); }
{plusOperator}					{ return token(sym.PLUS, yytext(), false); }
{ltOperator}					{ return token(sym.LT, yytext(), false); }
{lteqOperator}					{ return token(sym.LTEQ, yytext(), false); }
{gtOperator}					{ return token(sym.GT, yytext(), false); }
{gteqOperator} 					{ return token(sym.GTEQ, yytext(), false); }
{eqeqOperator} 					{ return token(sym.EQEQ, yytext(), false); }
{neqOperator} 					{ return token(sym.NEQ, yytext(), false); }
{andandOperator} 				{ return token(sym.AND, yytext(), false); }
{ororOperator} 					{ return token(sym.OR, yytext(), false); }
{eqOperator} 					{ return token(sym.EQ, yytext(), false); }

                                                           
/* structure */  

{leftBracesStracture}			{ return token(sym.LBRACE, yytext(), false); }
{rightBracesStracture}			{ return token(sym.RBRACE, yytext(), false); }
{semiStracture}					{ return token(sym.SEMI, yytext(), false); }
{comaStracture}					{ return token(sym.COMA, yytext(), false); }


/* comments */

{LineComment}             		{ /* ignore */ }         
"/*"                           	{ lastPos(); yybegin(TRADITIONAL_COMMENT); }     

                                                             
/* whitespace */

{WhiteSpace}                   	{ /* ignore */ }
}


<STRING> {
\"                             	{ yybegin(YYINITIAL); return token(sym.STRING_LITERAL, "STRING", string.toString(), true); }
{StringCharacter}+             	{ string.append(yytext()); }
"\\n"							{ string.append("\n"); }
"\\t"							{ string.append("\t"); }
"\\\""							{ string.append("\""); }
"\\\\"							{ string.append("\\"); }
<<EOF>>                        	{ Error(yytext(), true); }
}


<TRADITIONAL_COMMENT> {
[^\*]                          	{ /* ignore */ }
"*/"                           	{ yybegin(YYINITIAL); }
"*"                            	{ /* ignore */ }
<<EOF>>                        	{ Error(yytext(), true); }
}


/* error fallback */
[^]                           	{ Error(yytext(), false); }

/*regular EOF*/
<<EOF>>							{ return token(sym.EOF, yytext(), false); }

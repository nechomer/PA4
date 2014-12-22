package microLIR.parser;

import java_cup.runtime.*;

%%

%public
%cup
%class Lexer
%type Token
%line
%scanerror RuntimeException

%{
	public int getLineNumber() { return yyline+1; }
%}

Whitespace          = [ \t\r\n\f]+
newline = [\n\r]
DigitChar           = [0-9]
PosNum              = [1-9]
IdenChar            = [_0-9a-zA-Z]
VAR    				= [a-z]{IdenChar}*
REG					= R{IdenChar}*
Label				= _{IdenChar}+
NUMBER				= [0-9]+
PrintableNotSpecial = [\x20-\x21\x23-\x5B\x5D-\x7E]
Comment         	= ("#" [^\r\n\f]*)
String				= "\"" ({PrintableNotSpecial} | "\\\\" | "\\\"" | "\\t" | "\\n")* "\""

%%

"(" 			{ return new Token(yyline, yytext(), sym.LP); }
")" 			{ return new Token(yyline, yytext(), sym.RP); }
"[" 			{ return new Token(yyline, yytext(), sym.LB); }
"]" 			{ return new Token(yyline, yytext(), sym.RB); }
"." 			{ return new Token(yyline, yytext(), sym.DOT); }
":" 			{ return new Token(yyline, yytext(), sym.COLON); }
"," 			{ return new Token(yyline, yytext(), sym.COMMA); }
"=" 			{ return new Token(yyline, yytext(), sym.ASSIGN); }
"Move" 			{ return new Token(yyline, yytext(), sym.MOVE); }
"MoveArray" 	{ return new Token(yyline, yytext(), sym.MOVEARRAY); }
"ArrayLength" 	{ return new Token(yyline, yytext(), sym.ARRAYLENGTH); }
"MoveField" 	{ return new Token(yyline, yytext(), sym.MOVEFIELD); }
"Add" 			{ return new Token(yyline, yytext(), sym.ADD); }
"Sub" 			{ return new Token(yyline, yytext(), sym.SUB); }
"Mul" 			{ return new Token(yyline, yytext(), sym.MUL); }
"Div" 			{ return new Token(yyline, yytext(), sym.DIV); }
"Mod" 			{ return new Token(yyline, yytext(), sym.MOD); }
"Inc" 			{ return new Token(yyline, yytext(), sym.INC); }
"Dec" 			{ return new Token(yyline, yytext(), sym.DEC); }
"Neg" 			{ return new Token(yyline, yytext(), sym.NEG); }
"Not" 			{ return new Token(yyline, yytext(), sym.NOT); }
"And" 			{ return new Token(yyline, yytext(), sym.AND); }
"Or" 			{ return new Token(yyline, yytext(), sym.OR); }
"Xor" 			{ return new Token(yyline, yytext(), sym.XOR); }
"Compare"		{ return new Token(yyline, yytext(), sym.COMPARE); }
"Jump" 			{ return new Token(yyline, yytext(), sym.JUMP); }
"JumpTrue" 		{ return new Token(yyline, yytext(), sym.JUMPTRUE); }
"JumpFalse"		{ return new Token(yyline, yytext(), sym.JUMPFALSE); }
"JumpG" 		{ return new Token(yyline, yytext(), sym.JUMPG); }
"JumpGE"		{ return new Token(yyline, yytext(), sym.JUMPGE); }
"JumpL" 		{ return new Token(yyline, yytext(), sym.JUMPL); }
"JumpLE"		{ return new Token(yyline, yytext(), sym.JUMPLE); }
"VirtualCall"	{ return new Token(yyline, yytext(), sym.VIRTUALLCALL); }
"StaticCall" 	{ return new Token(yyline, yytext(), sym.STATICCALL); }
"Library" 		{ return new Token(yyline, yytext(), sym.LIBRARY); }
"Return" 		{ return new Token(yyline, yytext(), sym.RETURN); }
{NUMBER} 		{ return new Token(yyline, "NUMBER", sym.NUMBER, new Integer(yytext())); }
{VAR} 			{ return new Token(yyline, "VAR", sym.VAR, yytext()); }
{REG} 			{ return new Token(yyline, "REG", sym.REG, yytext()); }
{String}        { return new Token(yyline, "String", sym.STRING, yytext()); }
{Label}        	{ return new Token(yyline, "Label", sym.LABEL, yytext()); }
\n 				{}
\r 				{}
{Comment}       {                                                                        }
{Whitespace}    {                                                                        }
"//".*{newline} { }
. 				{ throw new RuntimeException("Illegal character at line " + (yyline+1) + " : '" + yytext() + "'"); }
<<EOF>> 		{ return new Token(yyline, "EOF", sym.EOF); }
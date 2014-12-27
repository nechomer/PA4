package IC.Parser;
import IC.Parser.sym;

%%

%class Lexer
%cup
%public
%function next_token
%type Token
%line
%scanerror LexicalError
%state STRING_STATE
%state STRING_SLASH_STATE
%state LINE_COMMENT_STATE
%state COMMENT_STATE
%state COMMENT_STAR_STATE

%{
	protected String stringHolder = "";
	public int getLineNumber() { return yyline+1; }
%}

%eofval{
	if(yystate() != YYINITIAL && yystate() != LINE_COMMENT_STATE)
	{
			if(yystate() == COMMENT_STATE){
				throw new LexicalError(yyline,"Reached End Of File without closing multi-line comment");
			}
			else if(yystate() == STRING_STATE){
				throw new LexicalError(yyline,"Reached End Of File without closing quoted string");
			}
			else{
	    		throw new LexicalError(yyline,"Unexpected end of file");
	    	}
	}
  	return new Token(yyline, sym.EOF);
%eofval}

ALPHA=[A-Za-z_]
DIGIT=[0-9]
ALPHA_NUMERIC={ALPHA}|{DIGIT}
UPPER=[A-Z]
LOWER=[a-z]
ID={LOWER}({ALPHA_NUMERIC})*
CLASS_IDENT={UPPER}({ALPHA_NUMERIC})*
NUMBER=({DIGIT})+
START_DIGIT=[1-9]


%%

<YYINITIAL> "(" 			{ return new Token(yyline, sym.LP); }
<YYINITIAL> ")" 			{ return new Token(yyline, sym.RP); }
<YYINITIAL> "=" 			{ return new Token(yyline, sym.ASSIGN); }
<YYINITIAL> "boolean" 		{ return new Token(yyline, sym.BOOLEAN); }
<YYINITIAL> "break" 		{ return new Token(yyline, sym.BREAK); }
<YYINITIAL> "class" 		{ return new Token(yyline, sym.CLASS); } 
<YYINITIAL> "," 			{ return new Token(yyline, sym.COMMA); }
<YYINITIAL> "continue" 		{ return new Token(yyline, sym.CONTINUE); }
<YYINITIAL> "/" 			{ return new Token(yyline, sym.DIVIDE); }
<YYINITIAL> "." 			{ return new Token(yyline, sym.DOT); }
<YYINITIAL> "==" 			{ return new Token(yyline, sym.EQUAL); }
<YYINITIAL> "extends" 		{ return new Token(yyline, sym.EXTENDS); }
<YYINITIAL> "else" 			{ return new Token(yyline, sym.ELSE); }
<YYINITIAL> "false" 		{ return new Token(yyline, sym.FALSE); }
<YYINITIAL> ">" 			{ return new Token(yyline, sym.GT); }
<YYINITIAL> ">=" 			{ return new Token(yyline, sym.GTE); }
<YYINITIAL> "if" 			{ return new Token(yyline, sym.IF); }
<YYINITIAL> "int" 			{ return new Token(yyline, sym.INT); }
<YYINITIAL> "&&" 			{ return new Token(yyline, sym.LAND); }
<YYINITIAL> "[" 			{ return new Token(yyline, sym.LB); }
<YYINITIAL> "{" 			{ return new Token(yyline, sym.LCBR); }
<YYINITIAL> "length" 		{ return new Token(yyline, sym.LENGTH); }
<YYINITIAL> "new" 			{ return new Token(yyline, sym.NEW); }
<YYINITIAL> "!" 			{ return new Token(yyline, sym.LNEG); }
<YYINITIAL> "||" 			{ return new Token(yyline, sym.LOR); }
<YYINITIAL> "<" 			{ return new Token(yyline, sym.LT); }
<YYINITIAL> "<=" 			{ return new Token(yyline, sym.LTE); }
<YYINITIAL> "-" 			{ return new Token(yyline, sym.MINUS); }
<YYINITIAL> "%" 			{ return new Token(yyline, sym.MOD); }
<YYINITIAL> "*" 			{ return new Token(yyline, sym.MULTIPLY); }
<YYINITIAL> "!=" 			{ return new Token(yyline, sym.NEQUAL); }
<YYINITIAL> "null" 			{ return new Token(yyline, sym.NULL); }
<YYINITIAL> "+" 			{ return new Token(yyline, sym.PLUS); }
<YYINITIAL> "]" 			{ return new Token(yyline, sym.RB); }
<YYINITIAL> "}" 			{ return new Token(yyline, sym.RCBR); }
<YYINITIAL> "return" 		{ return new Token(yyline, sym.RETURN); }
<YYINITIAL> ";" 			{ return new Token(yyline, sym.SEMI); }
<YYINITIAL> "static" 		{ return new Token(yyline, sym.STATIC); }
<YYINITIAL> "string" 		{ return new Token(yyline, sym.STRING); }

<YYINITIAL> "\""	 		{ yybegin(STRING_STATE); /* switch to string state */ 
							  stringHolder += "\"" ;}
<STRING_STATE> [^\n\t\"\\]*	{ stringHolder += yytext(); }
<STRING_STATE> "\""			{ String strTemp = stringHolder + "\"";
						  		stringHolder = "";
						  		yybegin(YYINITIAL);
						  		return new ValuedToken(yyline, sym.QUOTE, strTemp); }
<STRING_STATE> "\n"			{ throw new LexicalError(yyline, "Can't start a new line in the middle of a string."); }
<STRING_STATE> "\t"			{ throw new LexicalError(yyline, "Can't put a tab in the middle of a string."); }
<STRING_STATE> "\\"			{ yybegin( STRING_SLASH_STATE ); 
							  stringHolder += "\\"; }
<STRING_SLASH_STATE> [nt\\\"]	{ stringHolder += yytext(); 
							  yybegin( STRING_STATE ); }
<STRING_SLASH_STATE> .	{ throw new LexicalError(yyline, "Support only \\t, \\n, \\\\, \\\"  escape sequences."); }

<YYINITIAL> "this" 			{ return new Token(yyline, sym.THIS); }
<YYINITIAL> "true" 			{ return new Token(yyline, sym.TRUE); }
<YYINITIAL> "void" 			{ return new Token(yyline, sym.VOID); }
<YYINITIAL> "while" 		{ return new Token(yyline, sym.WHILE); }
<YYINITIAL> [\r\n\t ]   	{}

<YYINITIAL> "/*"			{ yybegin(COMMENT_STATE); }
<COMMENT_STATE> "*/"		{ yybegin(YYINITIAL); }
<COMMENT_STATE> .|"\n"		{}

<YYINITIAL> "//"			{yybegin(LINE_COMMENT_STATE);}
<LINE_COMMENT_STATE> [^\n] 	{}
<LINE_COMMENT_STATE> [\n] 	{yybegin(YYINITIAL);}

<YYINITIAL> {ID} 			{ return new ValuedToken(yyline, sym.ID, yytext()); }
<YYINITIAL> "0"+			{ return new ValuedToken(yyline, sym.INTEGER, yytext()); }
<YYINITIAL> "0"+([1-9])+  	{ throw new LexicalError(yyline, "leading zeroes are not allowed '"+yytext() +"'"); }
<YYINITIAL> {START_DIGIT}({NUMBER})*  		{ return new ValuedToken(yyline, sym.INTEGER, yytext()); }
<YYINITIAL> {CLASS_IDENT}	{ return new ValuedToken(yyline, sym.CLASS_ID, yytext() ); }
<YYINITIAL> .				{ throw new LexicalError(yyline, "illegal character '"+yytext() +"'"); }


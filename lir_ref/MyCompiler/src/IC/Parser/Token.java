package IC.Parser;

import java.util.HashMap;
import java.util.Map;

import java_cup.runtime.Symbol;

/**
 * Token class
 * Token represents an atomic building block of the IC program.
 * @author Asaf Bruner, Aviv Goll
 */
public class Token extends java_cup.runtime.Symbol {
		
	/**
	 * TokenNames maps between token ID and its name
	 */
	
    private static Map<Integer, String> TokenNames;
    static {
    	TokenNames = new HashMap<>();
    	TokenNames.put(new Integer(IC.Parser.sym.LP), "(");
    	TokenNames.put(new Integer(IC.Parser.sym.RP), ")");
    	TokenNames.put(new Integer(IC.Parser.sym.ASSIGN), "=");
    	TokenNames.put(new Integer(IC.Parser.sym.BOOLEAN), "boolean");
    	TokenNames.put(new Integer(IC.Parser.sym.BREAK), "break");
    	TokenNames.put(new Integer(IC.Parser.sym.CLASS), "class");
    	TokenNames.put(new Integer(IC.Parser.sym.CLASS_ID), "CLASS_ID");
    	TokenNames.put(new Integer(IC.Parser.sym.COMMA), ",");
    	TokenNames.put(new Integer(IC.Parser.sym.CONTINUE), "continue");
    	TokenNames.put(new Integer(IC.Parser.sym.DIVIDE), "/");
    	TokenNames.put(new Integer(IC.Parser.sym.DOT), ".");
    	TokenNames.put(new Integer(IC.Parser.sym.EQUAL), "==");
    	TokenNames.put(new Integer(IC.Parser.sym.EXTENDS), "extends");
    	TokenNames.put(new Integer(IC.Parser.sym.ELSE), "else");
    	TokenNames.put(new Integer(IC.Parser.sym.FALSE), "false");
    	TokenNames.put(new Integer(IC.Parser.sym.GT), ">");
    	TokenNames.put(new Integer(IC.Parser.sym.GTE), ">=");
    	TokenNames.put(new Integer(IC.Parser.sym.MOD), "%");
    	TokenNames.put(new Integer(IC.Parser.sym.MINUS), "-");
    	TokenNames.put(new Integer(IC.Parser.sym.LTE), "<=");
    	TokenNames.put(new Integer(IC.Parser.sym.LT), "<");
    	TokenNames.put(new Integer(IC.Parser.sym.LOR), "||");
    	TokenNames.put(new Integer(IC.Parser.sym.LNEG), "!");
    	TokenNames.put(new Integer(IC.Parser.sym.NEW), "new");
    	TokenNames.put(new Integer(IC.Parser.sym.LENGTH), "length");
    	TokenNames.put(new Integer(IC.Parser.sym.LCBR), "{");
    	TokenNames.put(new Integer(IC.Parser.sym.LB), "[");
    	TokenNames.put(new Integer(IC.Parser.sym.LAND), "&&");
    	TokenNames.put(new Integer(IC.Parser.sym.INTEGER), "INTEGER");
    	TokenNames.put(new Integer(IC.Parser.sym.INT), "int");
    	TokenNames.put(new Integer(IC.Parser.sym.IF), "if");
    	TokenNames.put(new Integer(IC.Parser.sym.ID), "ID");
    	TokenNames.put(new Integer(IC.Parser.sym.MULTIPLY), "*");
    	TokenNames.put(new Integer(IC.Parser.sym.NEQUAL), "!=");
    	TokenNames.put(new Integer(IC.Parser.sym.NULL), "null");
    	TokenNames.put(new Integer(IC.Parser.sym.PLUS), "+");
    	TokenNames.put(new Integer(IC.Parser.sym.RB), "]");
    	TokenNames.put(new Integer(IC.Parser.sym.RCBR), "}");
    	TokenNames.put(new Integer(IC.Parser.sym.RETURN), "return");
    	TokenNames.put(new Integer(IC.Parser.sym.SEMI), ";");
    	TokenNames.put(new Integer(IC.Parser.sym.STATIC), "static");
    	TokenNames.put(new Integer(IC.Parser.sym.STRING), "string");
    	TokenNames.put(new Integer(IC.Parser.sym.QUOTE), "QUOTE");
    	TokenNames.put(new Integer(IC.Parser.sym.THIS), "this");
    	TokenNames.put(new Integer(IC.Parser.sym.TRUE), "true");
    	TokenNames.put(new Integer(IC.Parser.sym.VOID), "void");
    	TokenNames.put(new Integer(IC.Parser.sym.WHILE), "while");
    	TokenNames.put(new Integer(IC.Parser.sym.EOF), "EOF");
    }
    
    private int id;
	
    /** 
     * Class constructor
     * @param line the line in which the token appeared.
     * @param id the ID of the token 
     * @see sym
     */
	public Token(int line, int id) {
        super(id, line+1, 0);
        this.id = id;
    }
	
	protected Token(int line, int id, Object value){
		super(id, line+1, 0, value);
		this.id = id;
	}
	
	/**
	 * A line getter
	 * @return line number
	 */
	public int getLine(){
		return left;
	}
	
	/**
	 * An ID getter
	 * @return ID
	 */
	public int getID(){
		return id;
	}
    
	
	@Override
	public String toString(){
		return TokenNames.get(id);
	}
	
}


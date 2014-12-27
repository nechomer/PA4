package IC.Parser;

public class SyntaxError extends Exception {

	/**
	 * Syntax Error class
	 * <p> This error is thrown whenever the parser encounters a syntax error.
	 * 
	 * @author Asaf Bruner, Aviv Goll
	 */
	private static final long serialVersionUID = 2330146797412209636L;
	private int line;

	/**
	 * Exception constructor
	 * @param string - the message to be conveyed
	 */
	public SyntaxError(int line, String string) {
		super(string);
		this.line = line;
	}

	public int getLine() {
		return line;
	}

}

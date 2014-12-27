package IC.Parser;

/**
 * Lexical Error class
 * <p> This error is thrown whenever the lexical analyzer fails to interpret the
 * program.
 * @author Asaf Bruner, Aviv Goll
 */
public class LexicalError extends Exception
{
	private static final long serialVersionUID = 6212043046195094107L;

	/**
	 * Line in which the analyzer failed.
	 */
	protected int lineNum=0; 
	
	/**
	 * Default exception constructor.
	 * According to the exercise requirements, this should not be used.
	 * It exists solely for the jflex default error macro.
	 * @param message Error message to be conveyed
	 */
	public LexicalError(String message) {
		super("Lexical error: "+message);
    }
	
	/**
	 * Exception constructor
	 * @param line The line in which the analyzer failed.
	 * @param message message to be conveyed
	 */
	public LexicalError(int line, String message) {
		super(Integer.toString(line+1)+": Lexical error: "+message);
		lineNum = line;
    }
}


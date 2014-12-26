package IC.Parser;

public class LexicalError extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LexicalError(int line, int column, String token) {
		super("Error!\t"+line+":"+" Lexical error: "+token);
    }
}


package IC.SemanticChecks;

public class SemanticError extends Exception {

	/**
	 * Semantic Error class
	 * <p> This error is thrown whenever the parser encounters a semantic error.
	 * 
	 * @author Asaf Bruner, Aviv Goll
	 */
	private static final long serialVersionUID = -8781278017645798621L;
	
	public SemanticError(int line, String string) {
		super("semantic error at line " + line + ": " + string);
	}

}

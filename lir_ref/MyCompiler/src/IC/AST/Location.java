package IC.AST;

/**
 * Abstract base class for variable reference AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Location extends Expression {

	private boolean lvalue = false;

	/**
	 * Constructs a new variable reference node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of reference.
	 */
	protected Location(int line) {
		super(line);
	}

	public void setLvalue(boolean b) {
		lvalue  = b;
		
	}
	
	public boolean isLvalue() {
		return lvalue;
	}
}

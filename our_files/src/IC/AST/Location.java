package IC.AST;


/**
 * Abstract base class for variable reference AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Location extends Expression {
	private boolean lhs = false;

	/**
	 * Constructs a new variable reference node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of reference.
	 */
	protected Location(int line) {
		super(line);
	}
	
	public void setLhs(boolean lhs) {
		this.lhs  = lhs;
		
	}
	
	public boolean isLhs() {
		return lhs;
	}

}

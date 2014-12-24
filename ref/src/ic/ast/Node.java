package ic.ast;


/**
 * Abstract AST node base class.
 */
public abstract class Node {

	private int line;
	
	public ic.sem.ScopeNode scope;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 */
	public abstract Object accept(Visitor visitor);

	/**
	 * Constructs an AST node corresponding to a line number in the original
	 * code. Used by subclasses.
	 * 
	 * @param line
	 *            The line number.
	 */
	protected Node(int line) {
		this.line = line;
	}

	public int getLine() {
		return line;
	}

}

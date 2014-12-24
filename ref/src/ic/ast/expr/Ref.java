package ic.ast.expr;


/**
 * Abstract base class for variable reference AST nodes.
 * 
 */
public abstract class Ref extends Expression {

	/**
	 * Constructs a new variable reference node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of reference.
	 */
	protected Ref(int line) {
		super(line);
	}

}

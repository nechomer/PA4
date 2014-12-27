package IC.AST;

import IC.SymbolTable.SymbolTable;

/**
 * Abstract base class for expression AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Expression extends ASTNode {

	private SymbolTable typeScope = null;
	
	/**
	 * Constructs a new expression node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of expression.
	 */
	protected Expression(int line) {
		super(line);
	}

	public SymbolTable getTypeScope() {
		return typeScope;
	}

	public void setTypeScope(SymbolTable typeScope) {
		this.typeScope = typeScope;
	}
}
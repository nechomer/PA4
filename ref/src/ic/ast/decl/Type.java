package ic.ast.decl;

import ic.ast.Node;

/**
 * Abstract base class for data type AST nodes.
 * 
 */
public abstract class Type extends Node {

	/**
	 * Number of array indexes in data type. 
	 * For example, int[][] -> dimension = 2.
	 */
	private int dimension = 0;

	/**
	 * Constructs a new type node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 */
	protected Type(int line) {
		super(line);
	}

	public abstract String getDisplayName();

	public int getArrayDimension() {
		return dimension;
	}

	public void incrementDimension() {
		++dimension;
	}
}

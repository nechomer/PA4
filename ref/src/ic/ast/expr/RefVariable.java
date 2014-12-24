package ic.ast.expr;

import ic.ast.Visitor;

/**
 * Variable reference AST node.
 * 
 */
public class RefVariable extends Ref {

	private String name;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new variable reference node.
	 * 
	 * @param line
	 *            Line number of reference.
	 * @param name
	 *            Name of variable.
	 */
	public RefVariable(int line, String name) {
		super(line);
		this.name = name;
	}

	public String getName() {
		return name;
	}

}

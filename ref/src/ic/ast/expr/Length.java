package ic.ast.expr;

import ic.ast.Visitor;

/**
 * Array length expression AST node.
 * 
 */
public class Length extends Expression {

	private Expression array;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new array length expression node.
	 * 
	 * @param array
	 *            Expression representing an array.
	 */
	public Length(int line, Expression array) {
		super(line);
		this.array = array;
	}

	public Expression getArray() {
		return array;
	}

}

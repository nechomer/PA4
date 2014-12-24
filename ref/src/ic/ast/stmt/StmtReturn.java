package ic.ast.stmt;

import ic.ast.Visitor;
import ic.ast.expr.Expression;

/**
 * Return statement AST node.
 * 
 */
public class StmtReturn extends Statement {

	private Expression value = null;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new return statement node, with no return value.
	 * 
	 * @param line
	 *            Line number of return statement.
	 */
	public StmtReturn(int line) {
		super(line);
	}

	/**
	 * Constructs a new return statement node.
	 * 
	 * @param line
	 *            Line number of return statement.
	 * @param value
	 *            Return value.
	 */
	public StmtReturn(int line, Expression value) {
		this(line);
		this.value = value;
	}

	public boolean hasValue() {
		return (value != null);
	}

	public Expression getValue() {
		return value;
	}

}

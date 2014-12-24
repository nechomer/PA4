package ic.ast.stmt;

import ic.ast.Visitor;

/**
 * Break statement AST node.
 * 
 */
public class StmtBreak extends Statement {

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a break statement node.
	 * 
	 * @param line
	 *            Line number of break statement.
	 */
	public StmtBreak(int line) {
		super(line);
	}

}

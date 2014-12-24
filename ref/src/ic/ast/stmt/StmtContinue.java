package ic.ast.stmt;

import ic.ast.Visitor;

/**
 * Continue statement AST node.
 * 
 */
public class StmtContinue extends Statement {

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a continue statement node.
	 * 
	 * @param line
	 *            Line number of continue statement.
	 */
	public StmtContinue(int line) {
		super(line);
	}

}

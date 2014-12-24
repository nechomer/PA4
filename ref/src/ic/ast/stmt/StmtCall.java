package ic.ast.stmt;

import ic.ast.Visitor;
import ic.ast.expr.Call;

/**
 * Method call statement AST node.
 * 
 */
public class StmtCall extends Statement {

	private Call call;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new method call statement node.
	 * 
	 * @param call
	 *            Method call expression.
	 */
	public StmtCall(Call call) {
		super(call.getLine());
		this.call = call;
	}

	public Call getCall() {
		return call;
	}

}

package ic.ast.stmt;

import ic.ast.Visitor;
import ic.ast.expr.Expression;

/**
 * If statement AST node.
 * 
 */
public class StmtIf extends Statement {

	private Expression condition;

	private Statement operation;

	private Statement elseOperation = null;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs an If statement node.
	 * 
	 * @param condition
	 *            Condition of the If statement.
	 * @param operation
	 *            Operation to perform if condition is true.
	 * @param elseOperation
	 *            Operation to perform if condition is false.
	 */
	public StmtIf(Expression condition, Statement operation, Statement elseOperation) {
		this(condition, operation);
		this.elseOperation = elseOperation;
	}

	/**
	 * Constructs an If statement node, without an Else operation.
	 * 
	 * @param condition
	 *            Condition of the If statement.
	 * @param operation
	 *            Operation to perform if condition is true.
	 */
	public StmtIf(Expression condition, Statement operation) {
		super(condition.getLine());
		this.condition = condition;
		this.operation = operation;
	}

	public Expression getCondition() {
		return condition;
	}

	public Statement getOperation() {
		return operation;
	}

	public boolean hasElse() {
		return (elseOperation != null);
	}

	public Statement getElseOperation() {
		return elseOperation;
	}

}

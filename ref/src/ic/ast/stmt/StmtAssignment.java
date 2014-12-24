package ic.ast.stmt;

import ic.ast.Visitor;
import ic.ast.expr.Expression;
import ic.ast.expr.Ref;

/**
 * Assignment statement AST node.
 * 
 */
public class StmtAssignment extends Statement {

	private Ref variable;

	private Expression assignment;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new assignment statement node.
	 * 
	 * @param variable
	 *            Variable to assign a value to.
	 * @param assignment
	 *            Value to assign.
	 */
	public StmtAssignment(Ref variable, Expression assignment) {
		super(variable.getLine());
		this.variable = variable;
		this.assignment = assignment;
	}

	public Ref getVariable() {
		return variable;
	}

	public Expression getAssignment() {
		return assignment;
	}

}

package ic.ast.expr;

import ic.ast.Visitor;


/**
 * Abstract base class for unary operation AST nodes.
 * 
 */
public class UnaryOp extends Expression {

	private UnaryOps operator;

	private Expression operand;

	@Override
	public Object accept(Visitor visitor)
	{
		return visitor.visit(this);
	}
	
	/**
	 * Constructs a new unary operation node. Used by subclasses.
	 * 
	 * @param operator
	 *            The operator.
	 * @param operand
	 *            The operand.
	 */
	public UnaryOp(int line, UnaryOps operator, Expression operand) {
		super(line);
		this.operator = operator;
		this.operand = operand;
	}

	public UnaryOps getOperator() {
		return operator;
	}

	public Expression getOperand() {
		return operand;
	}

	/**
	 * Enumerated type listing all possible unary operators.
	 */
	public enum UnaryOps {

		UMINUS("-", "negate"), 
		LNEG("!", "logical not");

		private String operator;
		private String description;

		private UnaryOps(String operator, String description) {
			this.operator = operator;
			this.description = description;
		}

		public String toString()       { return operator; }
		public String getDescription() {  return description; }

		public static UnaryOps find(String op)
		{
			for (UnaryOp.UnaryOps x : UnaryOp.UnaryOps.values()) {
				if (x.toString().equals(op)) return x;
			}
			throw new Error("internal error; unary operator not found: " + op);
		}
	}
	
}

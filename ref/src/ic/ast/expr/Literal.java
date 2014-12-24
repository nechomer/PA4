package ic.ast.expr;

import ic.ast.Visitor;
import ic.ast.decl.PrimitiveType;

/**
 * Literal value AST node.
 * 
 */
public class Literal extends Expression {

	private PrimitiveType.DataType type;

	private Object value;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new literal node, with a value.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 * @param value
	 *            Value of literal.
	 */
	public Literal(int line, PrimitiveType.DataType type, Object value) {
		super(line);
		this.type = type;
		this.value = value;
	}

	public PrimitiveType.DataType getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

}

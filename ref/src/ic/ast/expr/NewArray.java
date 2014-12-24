package ic.ast.expr;

import ic.ast.Visitor;
import ic.ast.decl.Type;

/**
 * Array creation AST node.
 * 
 */
public class NewArray extends New {

	private Type type;

	private Expression size;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new array creation expression node.
	 * 
	 * @param type
	 *            Data type of new array.
	 * @param size
	 *            Size of new array.
	 */
	public NewArray(Type type, Expression size) {
		super(type.getLine());
		this.type = type;
		this.size = size;
	}

	public Type getType() {
		return type;
	}

	public Expression getSize() {
		return size;
	}

}

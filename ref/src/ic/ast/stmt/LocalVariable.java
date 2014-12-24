package ic.ast.stmt;

import ic.ast.Visitor;
import ic.ast.decl.Type;
import ic.ast.expr.Expression;

/**
 * Local variable declaration statement AST node.
 * 
 */
public class LocalVariable extends Statement {

	private Type type;

	private String name;

	private Expression initial = null;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new local variable declaration statement node.
	 * 
	 * @param line
	 * 			  The line where the variable declaration occurs.
	 * @param type
	 *            Data type of local variable.
	 * @param name
	 *            Name of local variable.
	 */
	public LocalVariable(int line, Type type, String name) {
		super(line);
		this.type = type;
		this.name = name;
	}

	/**
	 * Constructs a new local variable declaration statement node, with an
	 * initial value.
	 * 
	 * @param line
	 * 			  The line where the variable declaration occurs.
	 * @param type
	 *            Data type of local variable.
	 * @param name
	 *            Name of local variable.
	 * @param initValue
	 *            Initial value of local variable.
	 */
	public LocalVariable(int line, Type type, String name, Expression initValue) {
		this(line, type, name);
		this.initial = initValue;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean isInitialized() {
		return (initial != null);
	}

	public Expression getInitialValue() {
		return initial;
	}

}

package ic.ast.expr;

import ic.ast.Visitor;

import java.util.List;

/**
 * Static method call AST node.
 * 
 */
public class StaticCall extends Call {

	private String className;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new static method call node.
	 * 
	 * @param line
	 *            Line number of call.
	 * @param className
	 *            Name of method's class.
	 * @param methodName
	 *            Name of method.
	 * @param arguments
	 *            List of all method arguments.
	 */
	public StaticCall(int line, String className, String methodName,
			List<Expression> arguments) {
		super(line, methodName, arguments);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}

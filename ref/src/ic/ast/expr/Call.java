package ic.ast.expr;


import java.util.List;

/**
 * Abstract base class for method call AST nodes.
 * 
 */
public abstract class Call extends Expression {

	private String method;

	private List<Expression> arguments;

	/**
	 * Constructs a new method call node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of call.
	 * @param methodName
	 *            Name of method.
	 * @param arguments
	 *            List of all method arguments.
	 */
	protected Call(int line, String methodName, List<Expression> arguments) {
		super(line);
		this.method = methodName;
		this.arguments = arguments;
	}

	public String getMethod() {
		return method;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

}

package ic.ast.decl;

import ic.ast.Node;
import ic.ast.stmt.Statement;

import java.util.List;

/**
 * Abstract base class for method AST nodes.
 * 
 */
public abstract class DeclMethod extends Node {

	protected Type type;

	protected String name;

	protected List<Parameter> formals;

	protected List<Statement> statements;

	/**
	 * Constructs a new method node. Used by subclasses.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 * @param statements
	 *            List of method's statements.
	 */
	protected DeclMethod(Type type, String name, List<Parameter> formals,
			List<Statement> statements) {
		super(type.getLine());
		this.type = type;
		this.name = name;
		this.formals = formals;
		this.statements = statements;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public List<Parameter> getFormals() {
		return formals;
	}

	public List<Statement> getStatements() {
		return statements;
	}
}

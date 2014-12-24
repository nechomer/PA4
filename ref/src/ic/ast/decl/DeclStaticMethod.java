package ic.ast.decl;

import ic.ast.Visitor;
import ic.ast.stmt.Statement;

import java.util.List;

/**
 * Static method AST node.
 * 
 */
public class DeclStaticMethod extends DeclMethod {

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new static method node.
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
	public DeclStaticMethod(Type type, String name, List<Parameter> formals,
			List<Statement> statements) {
		super(type, name, formals, statements);
	}

}

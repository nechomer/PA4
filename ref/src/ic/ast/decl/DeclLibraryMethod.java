package ic.ast.decl;

import ic.ast.Visitor;
import ic.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Library method declaration AST node.
 * 
 */
public class DeclLibraryMethod extends DeclMethod {

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new library method declaration node.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 */
	public DeclLibraryMethod(Type type, String name, List<Parameter> formals) {
		super(type, name, formals, new ArrayList<Statement>());
	}
}
